package com.multiply.esl_interface.v1.web.service;

import com.multiply.esl_interface.v1.global.common.converter.EncodingConverter;
import com.multiply.esl_interface.v1.web.mapper.aimscore.AimsCoreMapper;
import com.multiply.esl_interface.v1.web.mapper.oracle.AimsInterfaceLogMapper;
import com.multiply.esl_interface.v1.web.mapper.oracle.EslInterfaceMapper;
import com.multiply.esl_interface.v1.web.mapper.oracle.SsgDepartMapper;
import com.multiply.esl_interface.v1.web.model.*;
import com.multiply.esl_interface.v1.web.repository.TetrplEslRepository;
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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Component
public class EslInterfaceService {

    private final EslInterfaceMapper eslInterfaceMapper;
    private final SsgDepartMapper ssgdepartMapper;
    private final AimsInterfaceLogMapper aimsInterfaceLogMapper;
    private final AimsCoreMapper aimsCoreMapper;
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
        Map<String, Object> paramMap = new HashMap<>();
        List<String> stationCodes = aimsCoreMapper.selectStationCodes("ASC");
        paramMap.put("strCodes", stationCodes);

        while (true)
        {
            try {
                PagingRequest pagingRequest = PagingRequest.builder().startNum(dataCount+1).endNum(dataCount+ROW_PER_TRY).count(ROW_PER_TRY).build();

                paramMap.put("count", pagingRequest.getCount());
                paramMap.put("startNum", pagingRequest.getStartNum());

                List<TetpluEsl> tetpluEslSubList = ssgdepartMapper.selectVTetpluEsl(paramMap);

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

                int totalCnt = tetpluEslList.size();
                List<String> ifResultList = new ArrayList<>();

                // dat 파일 복수 처리(241123~)
                List<String> fileNameList = createCsvFileList(tetpluEslList);
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

                    aimsInterfaceLogMapper.save(interfaceLog);
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
            Map<String, Object> paramMap = new HashMap<>();

            List<String> stationCodes = aimsCoreMapper.selectStationCodes("ASC");
            paramMap.put("strCodes", stationCodes);

            List<TetrplEslA> tetrplEslAList = ssgdepartMapper.selectVTetrplEslA(paramMap); // AIMS insert용 조회
            if (tetrplEslAList.size() > 0) {
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
                        .failedPluCodes(String.join(",", failedPluCodeList))
                        .build();

                    aimsInterfaceLogMapper.save(interfaceLog);
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
     * .csv 파일(복수) 생성
     * @param rows 전체 데이터 목록
     * @return 생성된 파일명 목록
     * @throws IOException
     */
    public List<String> createCsvFileList(List<TetpluEsl> rows) throws IOException {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<String> fileNameList = new ArrayList<>();

        int rowCnt = rows.size();
        int chunkSize = 10000; // 1만 건씩 분할

        if (rowCnt > chunkSize) {
            for (int i = 0; i < rows.size(); i += chunkSize) {
                List<TetpluEsl> chunk = rows.subList(i, Math.min(i + chunkSize, rows.size()));

                String fileName = dateStr + "_" + (i / chunkSize + 1) + ".csv";
                createCsvFile(chunk, fileName, String.valueOf(i / chunkSize + 1)); // CSV 전용 메서드 호출
                fileNameList.add(fileName);
            }
        } else {
            String fileName = dateStr + "_1.csv";
            createCsvFile(rows, fileName, "1");
            fileNameList.add(fileName);
        }

        return fileNameList;
    }

    private String sanitize(Object value) {
        if (value == null) return "";
        return value.toString().replace("\n", " ").replace("\t", " ").replace(";", " ");
    }

    public String createCsvFile(List<TetpluEsl> rows, String fileName, String order) throws IOException {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String outputFilePath = path + dateStr + "_" + order + ".csv";

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8")) {
            writer.write(String.join("|", Arrays.asList(
                    "STORE_CODE", "PLU_CODE", "PRICE_SECT", "START_DATE", "END_DATE", "MD_CODE", "PUM_CODE", "EVENT_SECT", "CLASS_CODE", "PLU_NAME",
                    "CURR_SAL_PRICE", "GOODS_SECT", "MG_RATE", "TAX_FLAG", "SYS_DATE", "SYS_TIME", "ORIGIN_NAME", "NOR_SAL_PRICE", "CONTENTS_QTY",
                    "DISPLAY_UNIT_NAME", "DISPLAY_UNIT_QTY", "DEAL_GUBN", "ORIGIN_CODE", "BRAND_NAME", "CATE_CODE", "CATE_NAME",
                    "MOD_DATE", "MOD_TIME", "MOD_EMPNO", "IF_DATE", "IF_TIME", "IF_EMPNO", "DISPLAY_UNIT_PRICE", "SELLNG_PNT", "GOOS_AUTH_IMG",
                    "WINE_SUGR_CONT", "WINE_BODY", "WINE_KIND", "WINE_EVAL1", "WINE_EVAL2", "WINE_EVAL3", "WINE_PLOR", "WINE_ITEM_KIND", "WINE_VINT",
                    "MOBL_CUP_GOOS_YN", "LAYOUT_GUBN1", "LAYOUT_GUBN2", "PLOR_ORIGIN_NAME", "QR_CODE", "CONTENTS_QTY_UNIT", "COMBINE_DISPLAY_UNIT_PRICE",
                    "DISPLAY_TYPE", "GOOS_INFO_CD1", "GOOS_INFO_CD2", "UNIT_PRICE_STR", "EVENT_PERIOD_STR"
            )) + "\n");

            for (TetpluEsl row : rows) {
                // 필드 정제
                String storeCode = sanitize(row.getStoreCode());
                String pluCode = sanitize(row.getPluCode());
                String priceSect = sanitize(row.getPriceSect());
                String startDate = sanitize(row.getStartDate());
                String endDate = sanitize(row.getEndDate());
                String mdCode = sanitize(row.getMdCode());
                String pumCode = sanitize(row.getPumCode());
                String eventSect = sanitize(row.getEventSect());
                String classCode = sanitize(row.getClassCode());
                String pluName = sanitize(row.getPluName());
                String currSalPrice = sanitize(row.getCurrSalPrice());
                String goodsSect = sanitize(row.getGoodsSect());
                String mgRate = sanitize(row.getMgRate());
                String taxFlag = sanitize(row.getTaxFlag());
                String sysDate = sanitize(row.getSysDate());
                String sysTime = sanitize(row.getSysTime());
                String originName = sanitize(row.getOriginName());
                String norSalPrice = sanitize(row.getNorSalPrice());
                String contentsQty = sanitize(row.getContentsQty());
                String displayUnitName = sanitize(row.getDisplayUnitName());
                String displayUnitQty = sanitize(row.getDisplayUnitQty());
                String dealGubn = sanitize(row.getDealGubn());
                String originCode = sanitize(row.getOriginCode());
                String brandName = sanitize(row.getBrandName());
                String cateCode = sanitize(row.getCateCode());
                String cateName = sanitize(row.getCateName());
                String modDate = sanitize(row.getModDate());
                String modTime = sanitize(row.getModTime());
                String modEmpno = sanitize(row.getModEmpno());
                String ifDate = sanitize(row.getIfDate());
                String ifTime = sanitize(row.getIfTime());
                String ifEmpno = sanitize(row.getIfEmpno());
                String displayUnitPrice = sanitize(row.getDisplayUnitPrice());
                String sellngPnt = sanitize(row.getSellngPnt());
                String goosAuthImg = sanitize(row.getGoosAuthImg());
                String wineSugrCont = sanitize(row.getWineSugrCont());
                String wineBody = sanitize(row.getWineBody());
                String wineKind = sanitize(row.getWineKind());
                String wineEval1 = sanitize(row.getWineEval1());
                String wineEval2 = sanitize(row.getWineEval2());
                String wineEval3 = sanitize(row.getWineEval3());
                String winePlor = sanitize(row.getWinePlor());
                String wineItemKind = sanitize(row.getWineItemKind());
                String wineVint = sanitize(row.getWineVint());
                String moblCupGoosYn = sanitize(row.getMoblCupGoosYn());
                String layoutGubn1 = String.format("%4s", sanitize(row.getLayoutGubn1())).replaceAll(" ", "0");
                String layoutGubn2 = String.format("%4s", sanitize(row.getLayoutGubn2())).replaceAll(" ", "0");
                String plorOriginName = sanitize(row.getPlorOriginName());
                String qrCode = sanitize(row.getQrCode());
                String contentsQtyUnit = sanitize(row.getContentsQtyUnit());
                String combineDisplayUnitPrice = sanitize(row.getCombineDisplayUnitPrice());
                String goosInfoImg = sanitize(row.getGoosInfoImg());

                // 추가속성 추출
                List<String> addProps = Arrays.stream(goosInfoImg.split("\\|"))
                        .map(String::trim)
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());
                String goosInfoCd1 = addProps.size() > 0 ? addProps.get(0) : "";
                String goosInfoCd2 = addProps.size() > 1 ? addProps.get(1) : "";

                // 계산 필드
                String unitPriceStr = makeUnitPriceString(row);
                String eventPeriodStr = makeEventPeriodString(row);
                String displayType = layoutGubn1 + layoutGubn2;

                // CSV 라인 작성
                String rowString = String.join("|", Arrays.asList(
                        storeCode, pluCode, priceSect, startDate, endDate, mdCode, pumCode, eventSect, classCode, pluName,
                        currSalPrice, goodsSect, mgRate, taxFlag, sysDate, sysTime, originName, norSalPrice, contentsQty,
                        displayUnitName, displayUnitQty, dealGubn, originCode, brandName, cateCode, cateName,
                        modDate, modTime, modEmpno, ifDate, ifTime, ifEmpno, displayUnitPrice, sellngPnt, goosAuthImg,
                        wineSugrCont, wineBody, wineKind, wineEval1, wineEval2, wineEval3, winePlor, wineItemKind, wineVint,
                        moblCupGoosYn, layoutGubn1, layoutGubn2, plorOriginName, qrCode, contentsQtyUnit, combineDisplayUnitPrice,
                        displayType, goosInfoCd1, goosInfoCd2, unitPriceStr, eventPeriodStr
                ));

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

    public List<TetpluEsl> test() {
        //java.util.Map paramMap = new java.util.HashMap();
        //List<TetrplEslA> test = ssgdepartMapper.selectVTetrplEslA(paramMap);
        List<TetpluEsl> test = eslInterfaceMapper.selectVTetrplEslTest();
        return test;
    }
}
