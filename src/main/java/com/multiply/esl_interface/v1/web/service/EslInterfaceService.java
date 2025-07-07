package com.multiply.esl_interface.v1.web.service;

import com.multiply.esl_interface.v1.global.common.converter.EncodingConverter;
import com.multiply.esl_interface.v1.web.mapper.EslInterfaceMapper;
import com.multiply.esl_interface.v1.web.model.*;
import com.multiply.esl_interface.v1.web.repository.AimsInterfaceLogRepository;
import com.multiply.esl_interface.v1.web.repository.TetpluEslRepository;
import com.multiply.esl_interface.v1.web.repository.TetrplEslRepository;
import com.multiply.esl_interface.v1.web.service.EslInterfaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
@Component
public class EslInterfaceService {

    private final EslInterfaceMapper eslInterfaceMapper;
    private final AimsInterfaceLogRepository aimsInterfaceLogRepository;
    private final TetpluEslRepository tetpluEslRepository;
    private final TetrplEslRepository tetrplEslRepository;

    private static final Logger logger = LoggerFactory.getLogger(EslInterfaceService.class);


    @Value("${api.url}")
    private String apiUrl;

    @Value("${api.store}")
    private String apiStore;

    private String apiCompany = "%EB%B0%B1%ED%99%94%EC%A0%90";

    @Value("${file.path}")
    private String path;

    private static final int ROW_PER_TRY = 10000;

    /**
     * 일반 데이터(V_TETPLU_ESL) 수신
     * @param
     * @return
     * @throws
     */
    public void receiveTetpluEsl() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType){
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };
        long dataCount = 0;
        int rowCount = 0;
        List<TetpluEsl> tetpluEslList = new ArrayList<>(); // 오라클에서 나눠서 조회한 데이터를 취합하기 위한 리스트

        while (true)
        {
            try {
                PagingRequest pagingRequest = PagingRequest.builder().startNum(dataCount+1).endNum(dataCount+ROW_PER_TRY).count(ROW_PER_TRY).build();
                List<TetpluEsl> tetpluEslSubList = eslInterfaceMapper.selectVTetpluEsl(pagingRequest);
                log.info(apiCompany + "IF_ESL_DPT0.V_TETPLU_ESL size : " + tetpluEslSubList.size());
                if (tetpluEslSubList.size() == 0) {
                    break;
                }
                tetpluEslList.addAll(tetpluEslSubList);
                dataCount += tetpluEslSubList.size();
            } catch (Exception e) {
                log.error("TETPLU+EST query error : " + e.getMessage(), e);
            }
        }
        try {
//            String fileName = createDatFile(); // .dat 파일 만들기
            if (tetpluEslList.size() > 0) {    
                for (TetpluEsl tetpluEsl : tetpluEslList) {
                    // 1. 오라클 데이터 한글 컨버팅
                    tetpluEsl.setPluName(EncodingConverter.bytesToString(tetpluEsl.getPluName()));
                    tetpluEsl.setOriginName(EncodingConverter.bytesToString(tetpluEsl.getOriginName()));
                    tetpluEsl.setBrandName(EncodingConverter.bytesToString(tetpluEsl.getBrandName()));
                    tetpluEsl.setCateName(EncodingConverter.bytesToString(tetpluEsl.getCateName()));
                    tetpluEsl.setSellngPnt(EncodingConverter.bytesToString(tetpluEsl.getSellngPnt()));
                    tetpluEsl.setWineEval1(EncodingConverter.bytesToString(tetpluEsl.getWineEval1()));
                    tetpluEsl.setWineEval2(EncodingConverter.bytesToString(tetpluEsl.getWineEval2()));
                    tetpluEsl.setWineEval3(EncodingConverter.bytesToString(tetpluEsl.getWineEval3()));
                    tetpluEsl.setWinePlor(EncodingConverter.bytesToString(tetpluEsl.getWinePlor()));
                    tetpluEsl.setWineItemKind(EncodingConverter.bytesToString(tetpluEsl.getWineItemKind()));
                    tetpluEsl.setWineVint(EncodingConverter.bytesToString(tetpluEsl.getWineVint()));
                    tetpluEsl.setDisplayUnitName(EncodingConverter.bytesToString(tetpluEsl.getDisplayUnitName()));
                }
                // 2. MongoDB TETPLU_ESL 테이블 저장
                try {
                    // MongoDB 저장
                    tetpluEslRepository.saveAll(tetpluEslList);
                } catch (Exception e) {
                    log.error(e,e);
                }

                int totalCnt = tetpluEslList.size();
                List<String> ifResultList = new ArrayList<>();

                // dat 파일 복수 처리(241123~)
                List<String> fileNameList = createDatFileList(tetpluEslList);
                for (String fileName : fileNameList) {

                    // 2. AIMS insert
                    int successCnt = 0;
                    int failCnt = 0;

                    List<String> failedPluCodeList = new ArrayList<>();

                    // 2-1. 파일 base64로 변환하기 TODO
                    String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    String filePath = path + fileName;
                    byte[] binary = getFileBinary(filePath);
                    String base64data = Base64.getEncoder().encodeToString(binary);

                    // 2-2. /articles/upload api 쏘기
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                    String baseUrl = apiUrl + "dashboardservice/common/articles/upload";
                    String store = apiStore;
                    String finalUrl = String.format("%s?store=%s", baseUrl, store);

                    URL url = new URL(finalUrl);
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    if (httpConn instanceof HttpsURLConnection) { // HttpsURLConnection일 경우만
                        ((HttpsURLConnection) httpConn).setHostnameVerifier((hostname, session) -> true); // 호스트명 검증 비활성화
                    }
                    httpConn.setRequestMethod("POST");
                    httpConn.setRequestProperty("Content-Type", "application/json; utf-8");
                    httpConn.setDoOutput(true);
                    httpConn.setConnectTimeout(10000);  // 연결 타임아웃: 10초
                    httpConn.setReadTimeout(10000);     // 읽기 타임아웃

                    // 요청 본문 생성
                    JSONObject jsonObject = new JSONObject();

                    jsonObject.put("contentType", "IMAGE");
                    jsonObject.put("fileName", fileName);
                    jsonObject.put("imgBase64", base64data);
                    jsonObject.put("pageIndex", 1);

                    try (OutputStreamWriter wr = new OutputStreamWriter(httpConn.getOutputStream(), "UTF-8")) {
                        wr.write(jsonObject.toString()); // jsonArray를 JSON 문자열로 변환하여 전송
                        wr.flush();
                    }

                    // Read response body
                    BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String ifResult = "";
                    int responseCode = httpConn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // AIMS insert 성공 시 successCnt +1
                        ifResult = "SUCCESS";
                    } else {
                        ifResult = "ERROR";
                    }

                    ifResultList.add(ifResult);

                    try {
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        String responseCodeStr = jsonResponse.getString("responseCode");
                        String responseMessage = jsonResponse.getString("responseMessage");
                        String customBatchId = jsonResponse.isNull("customBatchId") ? null : jsonResponse.getString("customBatchId");
                    } catch (JSONException e) {
                        log.error(e, e);
                    }

                }

                LocalDateTime now = LocalDateTime.now();
                String ifDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String ifTime = now.format(DateTimeFormatter.ofPattern("HHmmss"));

                String finalIfResult = "";
                if (ifResultList.contains("ERROR")) {
                    if (ifResultList.contains("SUCCESS")) {
                        finalIfResult = "PARTIAL_ERROR";
                    } else {
                        finalIfResult = "ERROR";
                    }
                } else {
                    finalIfResult = "SUCCESS";
                }

                // 2-3. AIMS_INTERFACE_LOG insert
                try {
                    AimsInterfaceLog interfaceLog = AimsInterfaceLog.builder()
                    .ifType("TETPLU_ESL")
                    .ifDate(ifDate)
                    .ifTime(ifTime)
                    .ifResult(finalIfResult)
                    .ifTotalCnt(totalCnt)
                    .build();

                    aimsInterfaceLogRepository.save(interfaceLog);
                    log.info("INTERFACE_LOG INSERT : " + interfaceLog.toString());
                } catch (Exception e) {
                    logger.error("receiveVTetrplEsl-Exception 발생: {}", e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            logger.error("receiveTetpluEsl-IOException 발생: {}", e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("receiveTetpluEsl-NoSuchAlgorithmException 발생: {}", e.getMessage(), e);
        } catch (KeyManagementException e) {
            logger.error("receiveTetpluEsl-KeyManagementException 발생: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("receiveTetpluEsl-Exception 발생: {}", e.getMessage(), e);
        }

    }


    /**
     * 긴급 데이터(V_TETRPL_ESL) 수신
     * @param
     * @return
     * @throws
     */
    public void receiveVTetrplEsl() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType){
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };

        try {

            List<TetrplEsl> tetrplEslList = eslInterfaceMapper.selectVTetrplEsl(); // mongoDB insert용 조회
            log.info("IF_ESL_DPT0.V_TETRPL_ESL size : {}", tetrplEslList.size());
            List<TetrplEslA> tetrplEslAList = eslInterfaceMapper.selectVTetrplEslA(); // AIMS insert용 조회
            log.info("IF_ESL_DPT0.V_TETRPL_ESL size : {}", tetrplEslList.size());

            if (tetrplEslList.size() > 0) {

                for (TetrplEsl tetrplEsl : tetrplEslList) {

                    // 1-1. 오라클 데이터 한글 컨버팅
                    tetrplEsl.setPluName(EncodingConverter.bytesToString(tetrplEsl.getPluName()));
                    tetrplEsl.setOriginName(EncodingConverter.bytesToString(tetrplEsl.getOriginName()));
                    tetrplEsl.setBrandName(EncodingConverter.bytesToString(tetrplEsl.getBrandName()));
                    tetrplEsl.setSellngPnt(EncodingConverter.bytesToString(tetrplEsl.getSellngPnt()));
                    tetrplEsl.setWineEval1(EncodingConverter.bytesToString(tetrplEsl.getWineEval1()));
                    tetrplEsl.setWineEval2(EncodingConverter.bytesToString(tetrplEsl.getWineEval2()));
                    tetrplEsl.setWineEval3(EncodingConverter.bytesToString(tetrplEsl.getWineEval3()));
                    tetrplEsl.setWinePlor(EncodingConverter.bytesToString(tetrplEsl.getWinePlor()));
                    tetrplEsl.setWineItemKind(EncodingConverter.bytesToString(tetrplEsl.getWineItemKind()));
                    tetrplEsl.setWineVint(EncodingConverter.bytesToString(tetrplEsl.getWineVint()));
                    tetrplEsl.setDisplayUnitName(EncodingConverter.bytesToString(tetrplEsl.getDisplayUnitName()));
                }
            // 1. MongoDB TETRPL_ESL 테이블 저장
                try {

                    tetrplEslRepository.saveAll(tetrplEslList);
    
                } catch (Exception e) {
                    logger.error("receiveVTetrplEsl-Exception 발생: {}", e.getMessage(), e);
                }

                // 2. AIMS insert
                int successCnt = 0;
                int failCnt = 0;
                int totalCnt = tetrplEslAList.size();
                List<String> failedPluCodeList = new ArrayList<>();
                for (TetrplEslA tetrplEslA : tetrplEslAList) {

                    // 2-1. 오라클 데이터 한글 컨버팅
                    tetrplEslA.setPluName(EncodingConverter.bytesToString(tetrplEslA.getPluName()));
                    tetrplEslA.setOriginName(EncodingConverter.bytesToString(tetrplEslA.getOriginName()));
                    tetrplEslA.setBrandName(EncodingConverter.bytesToString(tetrplEslA.getBrandName()));
                    tetrplEslA.setCateName(EncodingConverter.bytesToString(tetrplEslA.getCateName()));
                    tetrplEslA.setSellngPnt(EncodingConverter.bytesToString(tetrplEslA.getSellngPnt()));
                    tetrplEslA.setWineEval1(EncodingConverter.bytesToString(tetrplEslA.getWineEval1()));
                    tetrplEslA.setWineEval2(EncodingConverter.bytesToString(tetrplEslA.getWineEval2()));
                    tetrplEslA.setWineEval3(EncodingConverter.bytesToString(tetrplEslA.getWineEval3()));
                    tetrplEslA.setWinePlor(EncodingConverter.bytesToString(tetrplEslA.getWinePlor()));
                    tetrplEslA.setWineItemKind(EncodingConverter.bytesToString(tetrplEslA.getWineItemKind()));
                    tetrplEslA.setWineVint(EncodingConverter.bytesToString(tetrplEslA.getWineVint()));
                    tetrplEslA.setDisplayUnitName(EncodingConverter.bytesToString(tetrplEslA.getDisplayUnitName()));

                    // 2-2. /articles api 쏘기
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                    String baseUrl = apiUrl +"dashboardservice/common/articles";
                    String store = apiStore;
                    String company = apiCompany;
                    String finalUrl = String.format("%s?store=%s&company=%s", baseUrl, store, company);

                    URL url = new URL(finalUrl);
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    if (httpConn instanceof HttpsURLConnection) { // HttpsURLConnection일 경우만
                        ((HttpsURLConnection) httpConn).setHostnameVerifier((hostname, session) -> true); // 호스트명 검증 비활성화
                    }
                    httpConn.setRequestMethod("POST");
                    httpConn.setRequestProperty("Content-Type", "application/json; utf-8");
                    httpConn.setDoOutput(true);
                    httpConn.setConnectTimeout(10000);  // 연결 타임아웃: 10초
                    httpConn.setReadTimeout(10000);     // 읽기 타임아웃

                    // 요청 본문 생성
                    JSONObject jsonObject = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    JSONObject dataObject = new JSONObject();
                    dataObject.put("STORE_CODE", tetrplEslA.getStoreCode());
                    dataObject.put("PLU_CODE", tetrplEslA.getPluCode());
                    dataObject.put("PRICE_SECT", tetrplEslA.getPriceSect());
                    dataObject.put("START_DATE", tetrplEslA.getStartDate());
                    dataObject.put("END_DATE", tetrplEslA.getEndDate());
                    dataObject.put("MD_CODE", tetrplEslA.getMdCode());
                    dataObject.put("PUM_CODE", tetrplEslA.getPumCode());
                    dataObject.put("EVENT_SECT", tetrplEslA.getEventSect());
                    dataObject.put("CLASS_CODE", tetrplEslA.getClassCode());
                    dataObject.put("PLU_NAME", tetrplEslA.getPluName());
                    dataObject.put("CURR_SAL_PRICE", tetrplEslA.getCurrSalPrice());
                    dataObject.put("GOODS_SECT", tetrplEslA.getGoodsSect());
                    dataObject.put("MG_RATE", tetrplEslA.getMgRate());
                    dataObject.put("TAX_FLAG", tetrplEslA.getTaxFlag());
                    dataObject.put("SYS_DATE", tetrplEslA.getSysDate());
                    dataObject.put("SYS_TIME", tetrplEslA.getSysTime());
                    dataObject.put("ORIGIN_NAME", tetrplEslA.getOriginName());
                    dataObject.put("NOR_SAL_PRICE", tetrplEslA.getNorSalPrice());
                    dataObject.put("CONTENTS_QTY", tetrplEslA.getContentsQty());
                    dataObject.put("DISPLAY_UNIT_NAME", tetrplEslA.getDisplayUnitName());
                    dataObject.put("DISPLAY_UNIT_QTY", tetrplEslA.getDisplayUnitQty());
                    dataObject.put("DEAL_GUBUN", tetrplEslA.getDealGubn());
                    dataObject.put("ORIGIN_CODE", tetrplEslA.getOriginCode());
                    dataObject.put("BRAND_NAME", tetrplEslA.getBrandName());
                    dataObject.put("CATE_CODE", tetrplEslA.getCateCode());
                    dataObject.put("CATE_NAME", tetrplEslA.getCateName());
                    dataObject.put("MOD_DATE", tetrplEslA.getModDate());
                    dataObject.put("MOD_TIME", tetrplEslA.getModTime());
                    dataObject.put("MOD_EMPNO", tetrplEslA.getModEmpno());
                    dataObject.put("IF_DATE", tetrplEslA.getIfDate());
                    dataObject.put("IF_TIME", tetrplEslA.getIfTime());
                    dataObject.put("IF_EMPNO", tetrplEslA.getIfEmpno());
                    dataObject.put("DISPLAY_UNIT_PRICE", tetrplEslA.getDisplayUnitPrice());
                    dataObject.put("SELLNG_PNT", tetrplEslA.getSellngPnt());
                    dataObject.put("GOOS_AUTH_IMG", tetrplEslA.getGoosAuthImg());
                    dataObject.put("WINE_SUGR_CONT", tetrplEslA.getWineSugrCont());
                    dataObject.put("WINE_BODY", tetrplEslA.getWineBody());
                    dataObject.put("WINE_KIND", tetrplEslA.getWineKind());
                    dataObject.put("WINE_EVAL_1", tetrplEslA.getWineEval1());
                    dataObject.put("WINE_EVAL_2", tetrplEslA.getWineEval2());
                    dataObject.put("WINE_EVAL_3", tetrplEslA.getWineEval3());
                    dataObject.put("WINE_PLOR", tetrplEslA.getWinePlor());
                    dataObject.put("WINE_ITEM_KIND", tetrplEslA.getWineItemKind());
                    dataObject.put("WINE_VINT", tetrplEslA.getWineVint());
                    dataObject.put("MOBL_CUP_GOOS_YN", tetrplEslA.getMoblCupGoosYn());
                    dataObject.put("LAYOUT_GUBN_1", tetrplEslA.getLayoutGubn1());
                    dataObject.put("LAYOUT_GUBN_2", tetrplEslA.getLayoutGubn2());
                    dataObject.put("PLOR_ORIGIN_NAME", tetrplEslA.getPlorOriginName());
                    dataObject.put("CONTENTS_QTY_UNIT", tetrplEslA.getContentsQtyUnit());
                    dataObject.put("QR_CODE", tetrplEslA.getQrCode());
                    dataObject.put("COMBINE_DISPLAY_UNIT_PRICE", tetrplEslA.getCombineDisplayUnitPrice());
                    // 식품관용 코드 추가 @250214
                    dataObject.put("DISPLAY_TYPE", tetrplEslA.getLayoutGubn1() + tetrplEslA.getLayoutGubn2());
                    // 추가속성
                    String addPropStr = tetrplEslA.getGoosInfoImg() == null ? "" : tetrplEslA.getGoosInfoImg();
                    String[] addProps = addPropStr.split("\\|");
                    List<String> addPropList = new ArrayList<>();
                    for (String addProp : addProps) addPropList.add(addProp.trim());
                    Collections.sort(addPropList, Collections.reverseOrder());
                    dataObject.put("GOOS_INFO_CD1", addPropList.size() > 0 ? addPropList.get(0) : "");
                    dataObject.put("GOOS_INFO_CD2", addPropList.size() > 1 ? addPropList.get(1) : "");
                    // 단위가격 문자열
                    dataObject.put("UNIT_PRICE_STR", makeUnitPriceString(tetrplEslA));
                    // 행사기간
                    dataObject.put("EVENT_PERIOD", makeEventPeriodString(tetrplEslA));

                    jsonObject.put("articleId", tetrplEslA.getPluCode());
                    jsonObject.put("articleName", tetrplEslA.getPluName());
                    jsonObject.put("data", dataObject);
                    jsonObject.put("nfcUrl", "");
                    jsonArray.put(jsonObject);

                    try (OutputStreamWriter wr = new OutputStreamWriter(httpConn.getOutputStream(), "UTF-8")) {
                        wr.write(jsonArray.toString()); // jsonArray를 JSON 문자열로 변환하여 전송
                        wr.flush();
                    }

                    // Read response body
                    BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    int responseCode = httpConn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // AIMS insert 성공 시 successCnt +1
                        successCnt++;
                    } else {
                        // AIMS insert 실패 시 failCnt +1
                        failCnt++;
                        failedPluCodeList.add(tetrplEslA.getPluCode());
                    }
                }

                LocalDateTime now = LocalDateTime.now();
                String ifDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String ifTime = now.format(DateTimeFormatter.ofPattern("HHmmss"));

                String ifResult = "";
                if (totalCnt == successCnt) {
                    ifResult = "SUCCESS";
                } else if (totalCnt > successCnt) {
                    ifResult = "PARTIAL_SUCCESS";
                } else {
                    ifResult = "FAIL";
                }

                // 2-3. AIMS_INTERFACE_LOG insert
                try {
                    AimsInterfaceLog interfaceLog = AimsInterfaceLog.builder()
                        .ifType("TETRPL_ESL")
                        .ifDate(ifDate)
                        .ifTime(ifTime)
                        .ifResult(ifResult)
                        .ifTotalCnt(totalCnt)
                        .ifSuccessCnt(successCnt)
                        .ifFailCnt(failCnt)
                        .failedPluCodes(failedPluCodeList)
                        .build();

                    aimsInterfaceLogRepository.save(interfaceLog);
                    log.info("INTERFACE_LOG INSERT : " + interfaceLog.toString());
                } catch (Exception e) {
                    logger.error("receiveVTetrplEsl-AIMS_INTERFACE_LOG insert Exception 발생: {}", e.getMessage(), e);
                }
            }

        } catch (IOException e) {
            logger.error("receiveVTetrplEsl-IOException 발생: {}", e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("receiveVTetrplEsl-NoSuchAlgorithmException 발생: {}", e.getMessage(), e);
        } catch (KeyManagementException e) {
            logger.error("receiveVTetrplEsl-KeyManagementException 발생: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("receiveVTetrplEsl-Exception 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * .dat 파일(복수) 생성
     * @param
     * @return
     * @throws
     */
    public List<String> createDatFileList(List<TetpluEsl> rows) throws IOException {

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<String> fileNameList = new ArrayList<>();

        // List<TetpluEsl> rows = eslInterfaceMapper.selectVTetpluEsl();

        // rowCnt 계산해 dat 파일 여러개로 만들기(241120)
        int rowCnt = rows.size();
        int chunkSize = 10000;
        if (rowCnt > chunkSize) {
            for (int i = 0; i < rows.size(); i += chunkSize) {
                // 청크 생성
                List<TetpluEsl> chunk = rows.subList(i, Math.min(i + chunkSize, rows.size()));

                // 파일 생성
                String fileName = dateStr + "_" + (i / chunkSize + 1) + ".dat";
                createDatFile(chunk, fileName, String.valueOf(i / chunkSize + 1));
                fileNameList.add(fileName);
            }
        } else {
            String fileName = dateStr  + "_1" + ".dat";
            createDatFile(rows, fileName, "1");
            fileNameList.add(fileName);
        }

        return fileNameList;
    }


    /**
     * .dat 파일 생성
     * @param
     * @return
     * @throws
     */
    public String createDatFile(List<TetpluEsl> rows, String fileName, String order) throws IOException {

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String outputFilePath = path + dateStr + "_" + order + ".dat";
//        String fileName = dateStr + ".dat";

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8")) {
            for (TetpluEsl row : rows) {
                // 오라클 한글 데이터 컨버팅 => 앞에서 컨버틴 완료됨
                // row.setPluName(EncodingConverter.bytesToString(row.getPluName()));
                // row.setOriginName(EncodingConverter.bytesToString(row.getOriginName()));
                // row.setBrandName(EncodingConverter.bytesToString(row.getBrandName()));
                // row.setCateName(EncodingConverter.bytesToString(row.getCateName()));
                // row.setSellngPnt(EncodingConverter.bytesToString(row.getSellngPnt()));
                // row.setWineEval1(EncodingConverter.bytesToString(row.getWineEval1()));
                // row.setWineEval2(EncodingConverter.bytesToString(row.getWineEval2()));
                // row.setWineEval3(EncodingConverter.bytesToString(row.getWineEval3()));
                // row.setWinePlor(EncodingConverter.bytesToString(row.getWinePlor()));
                // row.setWineItemKind(EncodingConverter.bytesToString(row.getWineItemKind()));
                // row.setWineVint(EncodingConverter.bytesToString(row.getWineVint()));

                String storeCode = !isNull(row.getStoreCode()) ? row.getStoreCode().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String pluCode = !isNull(row.getPluCode()) ? row.getPluCode().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String priceSect = !isNull(row.getPriceSect()) ? row.getPriceSect().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String startDate = !isNull(row.getStartDate()) ? row.getStartDate().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String endDate = !isNull(row.getEndDate()) ? row.getEndDate().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String mdCode = !isNull(row.getMdCode()) ? row.getMdCode().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String pumCode = !isNull(row.getPumCode()) ? row.getPumCode().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String eventSect = !isNull(row.getEventSect()) ? row.getEventSect().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String classCode = !isNull(row.getClassCode()) ? row.getClassCode().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String pluName = !isNull(row.getPluName()) ? row.getPluName().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String currSalPrice = !isNull(String.valueOf(row.getCurrSalPrice())) ? row.getCurrSalPrice().toString() : "";
                String goodsSect = !isNull(row.getGoodsSect()) ? row.getGoodsSect().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String mgRate = !isNull(String.valueOf(row.getMgRate())) ? row.getMgRate().toString() : "";
                String taxFlag = !isNull(row.getTaxFlag()) ? row.getTaxFlag().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String sysDate = !isNull(row.getSysDate()) ? row.getSysDate().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String sysTime = !isNull(row.getSysTime()) ? row.getSysTime().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String originName = !isNull(row.getOriginName()) ? row.getOriginName().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String norSalPrice = !isNull(String.valueOf(row.getNorSalPrice())) ? row.getNorSalPrice().toString() : "";
                String contentsQty = !isNull(String.valueOf(row.getContentsQty())) ? row.getContentsQty().toString() : "";
                String displayUnitName = !isNull(row.getDisplayUnitName()) ? row.getDisplayUnitName().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String displayUnitQty = !isNull(String.valueOf(row.getDisplayUnitQty())) ? row.getDisplayUnitQty().toString() : "";
                String dealGubn = !isNull(row.getDealGubn()) ? row.getDealGubn().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String originCode = !isNull(row.getOriginCode()) ? row.getOriginCode().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String brandName = !isNull(row.getBrandName()) ? row.getBrandName().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String cateCode = !isNull(row.getCateCode()) ? row.getCateCode().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String cateName = !isNull(row.getCateName()) ? row.getCateName().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String modDate = !isNull(row.getModDate()) ? row.getModDate().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String modTime = !isNull(row.getModTime()) ? row.getModTime().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String modEmpno = !isNull(row.getModEmpno()) ? row.getModEmpno().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String ifDate = !isNull(row.getIfDate()) ? row.getIfDate().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String ifTime = !isNull(row.getIfTime()) ? row.getIfTime().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String ifEmpno = !isNull(row.getIfEmpno()) ? row.getIfEmpno().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String displayUnitPrice = !isNull(String.valueOf(row.getDisplayUnitPrice())) ? row.getDisplayUnitPrice().toString() : "";
                String sellngPnt = !isNull(row.getSellngPnt()) ? row.getSellngPnt().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String goosAuthImg = !isNull(row.getGoosAuthImg()) ? row.getGoosAuthImg().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String wineSugrCont = !isNull(row.getWineSugrCont()) ? row.getWineSugrCont().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String wineBody = !isNull(row.getWineBody()) ? row.getWineBody().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String wineKind = !isNull(row.getWineKind()) ? row.getWineKind().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String wineEval1 = !isNull(row.getWineEval1()) ? row.getWineEval1().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String wineEval2 = !isNull(row.getWineEval2()) ? row.getWineEval2().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String wineEval3 = !isNull(row.getWineEval3()) ? row.getWineEval3().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String winePlor = !isNull(row.getWinePlor()) ? row.getWinePlor().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String wineItemKind = !isNull(row.getWineItemKind()) ? row.getWineItemKind().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String wineVint = !isNull(row.getWineVint()) ? row.getWineVint().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String moblCupGoosYn = !isNull(row.getMoblCupGoosYn()) ? row.getMoblCupGoosYn().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String layoutGubn1 = !isNull(row.getLayoutGubn1()) ? row.getLayoutGubn1().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String layoutGubn2 = !isNull(row.getLayoutGubn2()) ? row.getLayoutGubn2().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String plorOriginName = !isNull(row.getPlorOriginName()) ? row.getPlorOriginName().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String qrCode = !isNull(row.getQrCode()) ? row.getQrCode().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String contentsQtyUnit = !isNull(row.getContentsQtyUnit()) ? row.getContentsQtyUnit().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String combineDisplayUnitPrice = !isNull(row.getCombineDisplayUnitPrice()) ? row.getCombineDisplayUnitPrice().toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
                String goosInfoImg = !isNull(row.getGoosInfoImg()) ? row.getGoosInfoImg().toString() : "";
                // 식품관용 코드 추가 @250214
                if (layoutGubn1 != null) {
                    layoutGubn1 = String.format("%4s", layoutGubn1).replaceAll(" ", "0");
                }
                if (layoutGubn2 != null) {
                    layoutGubn2 = String.format("%4s", layoutGubn2).replaceAll(" ", "0");
                }
        
                String DISPLAY_TYPE = layoutGubn1 + layoutGubn2;
                // 추가속성
                String[] addProps = goosInfoImg.split("\\|");
                List<String> addPropList = new ArrayList<>();
                for (String addProp : addProps) addPropList.add(addProp.trim());
                Collections.sort(addPropList, Collections.reverseOrder());
                String GOOS_INFO_CD1 = addPropList.size() > 0 ? addPropList.get(0) : "";
                String GOOS_INFO_CD2 = addPropList.size() > 1 ? addPropList.get(1) : "";
                // 단위가격 문자열 @250215 TODO 추가할것
                String unitPriceStr = makeUnitPriceString(row);
                // 행사기간 문자열
                String eventPeriodStr = makeEventPeriodString(row);
                // 코너 구분값
                String cornerStr = null;
                String rowString = String.join(";", storeCode, pluCode, priceSect, startDate, endDate, mdCode, pumCode, eventSect, classCode, pluName, currSalPrice, goodsSect, mgRate, taxFlag, sysDate, sysTime, originName, norSalPrice, contentsQty, displayUnitName, displayUnitQty, dealGubn, originCode, brandName, cateCode, cateName, modDate, modTime, modEmpno, ifDate, ifTime, ifEmpno, displayUnitPrice, sellngPnt, goosAuthImg, wineSugrCont, wineBody, wineKind, wineEval1, wineEval2, wineEval3, winePlor, wineItemKind, wineVint, moblCupGoosYn, layoutGubn1, layoutGubn2, plorOriginName, qrCode, contentsQtyUnit, combineDisplayUnitPrice, DISPLAY_TYPE, GOOS_INFO_CD1, GOOS_INFO_CD2, unitPriceStr, eventPeriodStr);
                // TODO CORNER 추가 후
                // String rowString = String.join(";", storeCode, pluCode, priceSect, startDate, endDate, mdCode, pumCode, eventSect, classCode, pluName, currSalPrice, goodsSect, mgRate, taxFlag, sysDate, sysTime, originName, norSalPrice, contentsQty, displayUnitName, displayUnitQty, dealGubn, originCode, brandName, cateCode, cateName, modDate, modTime, modEmpno, ifDate, ifTime, ifEmpno, displayUnitPrice, sellngPnt, goosAuthImg, wineSugrCont, wineBody, wineKind, wineEval1, wineEval2, wineEval3, winePlor, wineItemKind, wineVint, moblCupGoosYn, layoutGubn1, layoutGubn2, plorOriginName, qrCode, contentsQtyUnit, combineDisplayUnitPrice, DISPLAY_TYPE, GOOS_INFO_CD1, GOOS_INFO_CD2, unitPriceStr, eventPeriodStr, cornerStr);
                writer.write(rowString + "\n");
            }
        }
        log.info("outputFilePath : {}", outputFilePath);

        return fileName;
    }


    /**
     * .dat 파일 읽기
     * @param
     * @return
     * @throws
     */
    private static byte[] getFileBinary(String filepath) {
        File file = new File(filepath);
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream stream = new FileInputStream(file)) {
            stream.read(data, 0, data.length);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return data;
    }

    private boolean isNull(String str) {
        return str == null || str.trim().length() == 0 || "null".equals(str);
    }

    private static String makeUnitPriceString(TetrplEslA tetrplEslA) {
        int contentsQty = tetrplEslA.getContentsQty() != null ? tetrplEslA.getContentsQty() : 0;
        // String contentsQtyUnit = tetrplEslA.getContentsQtyUnit(); // 단위의 한글 깨짐이 의심되서 사용않음
        int displayUnitQty = tetrplEslA.getDisplayUnitQty() != null ? tetrplEslA.getDisplayUnitQty() : 0;
        String displayUnitName = tetrplEslA.getDisplayUnitName() != null ? tetrplEslA.getDisplayUnitName() : "";
        long displayUnitPrice = tetrplEslA.getDisplayUnitPrice() != null ? tetrplEslA.getDisplayUnitPrice() : 0;
        long combineDisplayUNitPrice = tetrplEslA.getCombineDisplayUnitPrice() != null ? tetrplEslA.getCombineDisplayUnitPrice() : 0;
        
        String volumeStr = (contentsQty == 0) ? "" : (contentsQty + displayUnitName);
        String unitStr = (combineDisplayUNitPrice == 0 && displayUnitPrice == 0) ? "" : (displayUnitQty + displayUnitName + "당" + String.format("%,d원", combineDisplayUNitPrice == 0 ? displayUnitPrice : combineDisplayUNitPrice));
        StringBuffer sb = new StringBuffer();
        if (volumeStr.length() > 0) {
            sb.append(volumeStr);
            if (unitStr.length() > 0) {
                sb.append(" / ");
            }
        }
        if (unitStr.length() > 0) {
            sb.append(unitStr);
        }
        return sb.toString();
    }
    private static String makeUnitPriceString(TetpluEsl tetrplEslA) {
        try {
            int contentsQty = tetrplEslA.getContentsQty() != null ? tetrplEslA.getContentsQty() : 0;
            // String contentsQtyUnit = tetrplEslA.getContentsQtyUnit(); // 단위의 한글 깨짐이 의심되서 사용않음
            int displayUnitQty = tetrplEslA.getDisplayUnitQty() != null ? tetrplEslA.getDisplayUnitQty() : 0;
            String displayUnitName = tetrplEslA.getDisplayUnitName() != null ? tetrplEslA.getDisplayUnitName() : "";
            long displayUnitPrice = tetrplEslA.getDisplayUnitPrice() != null ? tetrplEslA.getDisplayUnitPrice() : 0;
            long combineDisplayUNitPrice = 0;
            try {
                if (tetrplEslA.getCombineDisplayUnitPrice() != null && !"null".equalsIgnoreCase(tetrplEslA.getCombineDisplayUnitPrice())) {
                    combineDisplayUNitPrice = Long.parseLong(tetrplEslA.getCombineDisplayUnitPrice());
                }
            } catch (Exception e) {
                log.error("COMBINE_DISPLAY_UNIT_PRICE PARSE ERROR : {}", e.getMessage());
            }
            
            String volumeStr = (contentsQty == 0) ? "" : (contentsQty + displayUnitName);
            String unitStr = (combineDisplayUNitPrice == 0 && displayUnitPrice == 0) ? "" : (displayUnitQty + displayUnitName + new String("당".getBytes(), "UTF-8") + String.format("%,d", combineDisplayUNitPrice == 0 ? displayUnitPrice : combineDisplayUNitPrice) + new String("원".getBytes(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            if (volumeStr.length() > 0) {
                sb.append(volumeStr);
                if (unitStr.length() > 0) {
                    sb.append(" / ");
                }
            }
            if (unitStr.length() > 0) {
                sb.append(unitStr);
            }
            return sb.toString();    
        } catch (Exception e) {
            log.error("makeUnitPriceString ERROR : {}", e.getMessage());
        }
        return "";
    }
    private static String makeEventPeriodString(TetrplEslA tetrplEslA) {
        String periodStr = "";
        // if ("0003".equals(tetrplEslA.getLayoutGubn1())) {
            // 시작일
            String startDate = tetrplEslA.getStartDate() != null ? tetrplEslA.getStartDate().trim() : "";
            if (startDate.length() == 8) {
                startDate = startDate.substring(4, 6) + "." + startDate.substring(6, 8);
            }
            else if (startDate.length() == 4) {
                startDate = startDate.substring(0, 2) + "." + startDate.substring(2, 4);
            }
            // 종료일
            String endDate = tetrplEslA.getEndDate() != null ? tetrplEslA.getEndDate().trim() : "";
            if (endDate.length() == 8) {
                endDate = endDate.substring(4, 6) + "." + endDate.substring(6, 8);
            }
            else if (endDate.length() == 4) {
                endDate = endDate.substring(0, 2) + "." + endDate.substring(2, 4);
            }
            periodStr = startDate + "-" + endDate;
        // }
        return periodStr;
    }
    private static String makeEventPeriodString(TetpluEsl tetrplEslA) {
        String periodStr = "";
        // if ("0003".equals(tetrplEslA.getLayoutGubn1())) {
            // 시작일
            String startDate = tetrplEslA.getStartDate() != null ? tetrplEslA.getStartDate().trim() : "";
            if (startDate.length() == 8) {
                startDate = startDate.substring(4, 6) + "." + startDate.substring(6, 8);
            }
            else if (startDate.length() == 4) {
                startDate = startDate.substring(0, 2) + "." + startDate.substring(2, 4);
            }
            // 종료일
            String endDate = tetrplEslA.getEndDate() != null ? tetrplEslA.getEndDate().trim() : "";
            if (endDate.length() == 8) {
                endDate = endDate.substring(4, 6) + "." + endDate.substring(6, 8);
            }
            else if (endDate.length() == 4) {
                endDate = endDate.substring(0, 2) + "." + endDate.substring(2, 4);
            }
            periodStr = startDate + "-" + endDate;
        // }
        return periodStr;
    }
}
