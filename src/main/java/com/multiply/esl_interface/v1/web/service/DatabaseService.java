package com.multiply.esl_interface.v1.web.service;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${file.columns}")
    private String[] columns;

    public void executeQueryToFile(String outputFilePath) throws IOException {
        //TODO 리소스나 다른데 저장한 sql 불러서 변수화로 쓰고싶은데 인식 잘안댐
        //Resource resource = new ClassPathResource("queries/daily.sql");
        //InputStream inputStream = resource.getInputStream();
        //String query = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        // 직접 쿼리를 입력
        /*String query = "SELECT EPD.STR_CD AS STORE_CODE, " +
                "LPAD(EPD.PIPD_CD, 13, '0') AS PLU_CODE, " +
                "EPD.NRM_EVN_DVS_CD AS PRICE_SECT, " +
                "EPD.PRC_BGN_YMD AS START_DATE, " +
                "EPD.PRC_END_YMD AS END_DATE, " +
                "LPAD(EPD.MD_CD, 6, '0') AS MD_CODE, " +
                "LPAD(EPD.PDCT_CL_CD, 4, '0') AS PUM_CODE, " +
                "EPD.EVN_DVS_CD AS EVENT_SECT, " +
                "EPD.CPCO_CD AS CLASS_CODE, " +
                "REPLACE(REPLACE(REPLACE(EPD.PIPD_NM, '\\n', ' '), '\\t', ' '), ';', ' ') AS PLU_NAME, " +
                "EPD.NOW_SPRC_UPRC AS CURR_SAL_PRICE, " +
                "EPD.PIPD_DVS_CD AS GOODS_SECT, " +
                "EPD.MGRT AS MG_RATE, " +
                "EPD.TAX_DVS_CD AS TAX_FLAG, " +
                "DATE_FORMAT(EPD.REG_DTM ,'%Y%m%d') AS SYS_DATE, " +
                "DATE_FORMAT(EPD.REG_DTM, '%H%i') AS SYS_TIME, " +
                "EPD.OPL_NM AS ORIGIN_NAME, " +
                "EPD.NRM_SPRC_UPRC AS NOR_SAL_PRICE, " +
                "EPD.PDCT_TOT_CPC AS CONTENTS_QTY, " +
                "EPD.INDI_UNT_NM AS DISPLAY_UNIT_NAME, " +
                "EPD.INDI_UNT_QTY AS DISPLAY_UNIT_QTY, " +
                "EPD.DLNG_SHPE_CD AS DEAL_GUBN, " +
                "EPD.OPL_CD AS ORIGIN_CODE, " +
                "EPD.PDCT_BRD_NM AS BRAND_NAME, " +
                "EPD.ESL_CTG_CD AS CATE_CODE, " +
                "EPD.ESL_CTG_NM AS CATE_NAME, " +
                "DATE_FORMAT(EPD.UPD_DTM ,'%Y%m%d') AS MOD_DATE, " +
                "DATE_FORMAT(EPD.UPD_DTM, '%H%i') AS MOD_TIME, " +
                "EPD.ITF_HNDR_EMPNO AS MOD_EMPNO, " +
                "EPD.ITF_YMD AS IF_DATE, " +
                "EPD.ITF_TM AS IF_TIME, " +
                "EPD.ITF_HNDR_EMPNO AS IF_EMPNO, " +
                "EPD.NOW_SPRC_UPRC AS DISPLAY_UNIT_PRICE, " +
                "REPLACE(REPLACE(REPLACE(EPD.ESL_SLPNT_CNTN, '\\n', ' '), '\\t', ' '), ';', ' ') AS SELLNG_PNT, " +
                "EPD.IMG_YN AS GOOS_AUTH_IMG, " +
                "EPD.WINE_SGCN_DVS_CD AS WINE_SUGR_CONT, " +
                "EPD.WINE_WNBD_DVS_CD AS WINE_BODY, " +
                "EPD.WINE_TPE_CD AS WINE_KIND, " +
                "REPLACE(REPLACE(REPLACE(EPD.WINE_EVL_CNTN1, '\\n', ' '), '\\t', ' '), ';', ' ') AS WINE_EVAL_1, " +
                "EPD.WINE_EVL_CNTN2 AS WINE_EVAL_2, " +
                "EPD.WINE_EVL_CNTN3 AS WINE_EVAL_3, " +
                "ETB.WINE_OPL_NM AS WINE_PLOR, " +
                "EPD.WINE_PDTY_CNTN AS WINE_ITEM_KIND, " +
                "EPD.WINE_VNTG_CNTN AS WINE_VINT, " +
                "EPD.MBLE_CPN_PDCT_YN AS MOBL_CUP_GOOS_YN, " +
                "EPD.ESL_LYT_DVS_CD1 AS LAYOUT_GUBN_1, " +
                "EPD.ESL_LYT_DVS_CD2 AS LAYOUT_GUBN_2, " +
                "'' AS QR_CODE, " +
                "'' AS PLOR_ORIGIN_NAME, " +
                "CONCAT(EPD.PDCT_TOT_CPC, EPD.INDI_UNT_NM) AS CONTENTS_QTY_UNIT, " +
                "CASE " +
                "    WHEN EPD.PDCT_TOT_CPC IS NOT NULL AND EPD.PDCT_TOT_CPC > 0 THEN " +
                "        CASE " +
                "            WHEN TRIM(EPD.INDI_UNT_NM) = 'ml' THEN ROUND((EPD.NOW_SPRC_UPRC/EPD.PDCT_TOT_CPC) * 100) " +
                "            WHEN TRIM(EPD.INDI_UNT_NM) = 'l' THEN ROUND((EPD.NOW_SPRC_UPRC/EPD.PDCT_TOT_CPC) * 1000 * 100) " +
                "            ELSE NULL " +
                "        END " +
                "    ELSE NULL " +
                "END AS COMBINE_DISPLAY_UNIT_PRICE " +
                "FROM PR_ESL_PIPD_I EPD, " +
                "PR_PIPD_ESL_ATRB ETB " +
                "WHERE 1=1 " +
                "AND EPD.PIPD_CD = ETB.PIPD_CD " +
                "AND EPD.STR_CD = '14' " +
                "ORDER BY EPD.PIPD_CD ASC;";*/

        String query = "SELECT @rownum := @rownum + 1 AS rnum\n" +
                "     , A.STORE_CODE                                                                 AS STORE_CODE \n" +
                "     , LPAD(A.PLU_CODE, 13, '0')                                                    AS PLU_CODE \n" +
                "     , A.PRICE_SECT                                                                 AS PRICE_SECT \n" +
                "     , A.START_DATE                                                                 AS START_DATE \n" +
                "     , A.END_DATE                                                                   AS END_DATE \n" +
                "     , LPAD(A.MD_CODE, 6, '0')                                                      AS MD_CODE \n" +
                "     , LPAD(A.PUM_CODE, 4, '0')                                                     AS PUM_CODE \n" +
                "     , A.EVENT_SECT                                                                 AS EVENT_SECT \n" +
                "     , A.CLASS_CODE                                                                 AS CLASS_CODE \n" +
                "     , REPLACE(REPLACE(REPLACE(A.PLU_NAME, '\\n', ' '), '\\t', ' '), ';', ' ')      AS PLU_NAME \n" +
                "     , A.CURR_SAL_PRICE                                                             AS CURR_SAL_PRICE     \n" +
                "     , A.GOODS_SECT                                                                 AS GOODS_SECT \n" +
                "     , A.MG_RATE                                                                    AS MG_RATE \n" +
                "     , ''                                                                           AS TAX_FLAG \n" +
                "     , DATE_FORMAT(A.SYS_DATE ,'%Y%m%d')                                            AS SYS_DATE \n" +
                "     , ''                                                                           AS SYS_TIME \n" +
                "     , A.ORIGIN_NAME                                                                AS ORIGIN_NAME \n" +
                "     , A.NOR_SAL_PRICE                                                              AS NOR_SAL_PRICE \n" +
                "     , A.CONTENTS_QTY                                                               AS CONTENTS_QTY \n" +
                "     , A.DISPLAY_UNIT_NAME                                                          AS DISPLAY_UNIT_NAME \n" +
                "     , A.DISPLAY_UNIT_QTY                                                           AS DISPLAY_UNIT_QTY \n" +
                "     , A.DEAL_GUBN                                                                  AS DEAL_GUBN \n" +
                "     , A.ORIGIN_CODE                                                                AS ORIGIN_CODE \n" +
                "     , A.BRAND_NAME                                                                 AS BRAND_NAME \n" +
                "     , A.CATE_CODE                                                                  AS CATE_CODE \n" +
                "     , A.CATE_NAME                                                                  AS CATE_NAME \n" +
                "     , DATE_FORMAT(A.MOD_DATE ,'%Y%m%d')                                            AS MOD_DATE \n" +
                "     , ''                                                                           AS MOD_TIME \n" +
                "     , ''                                                                           AS MOD_EMPNO \n" +
                "     , A.IF_DATE                                                                    AS IF_DATE \n" +
                "     , A.IF_TIME                                                                    AS IF_TIME \n" +
                "     , ''                                                                           AS IF_EMPNO \n" +
                "     , ''                                                                           AS DISPLAY_UNIT_PRICE \n" +
                "     , REPLACE(REPLACE(REPLACE(A.SELLNG_PNT, '\\n', ' '), '\\t', ' '), ';', ' ')    AS SELLNG_PNT \n" +
                "     , A.GOOS_AUTH_IMG                                                              AS GOOS_AUTH_IMG \n" +
                "     , A.WINE_SUGR_CONT                                                             AS WINE_SUGR_CONT \n" +
                "     , A.WINE_BODY                                                                  AS WINE_BODY \n" +
                "     , A.WINE_KIND                                                                  AS WINE_KIND \n" +
                "     , REPLACE(REPLACE(REPLACE(A.WINE_EVAL_1, '\\n', ' '), '\\t', ' '), ';', ' ')   AS WINE_EVAL_1 \n" +
                "     , A.WINE_EVAL_2                                                                AS WINE_EVAL_2 \n" +
                "     , A.WINE_EVAL_3                                                                AS WINE_EVAL_3 \n" +
                "     , ''                                                                           AS WINE_PLOR \n" +
                "     , A.WINE_ITEM_KIND                                                             AS WINE_ITEM_KIND \n" +
                "     , A.WINE_VINT                                                                  AS WINE_VINT \n" +
                "     , A.MOBL_CUP_GOOS_YN                                                           AS MOBL_CUP_GOOS_YN \n" +
                "     , A.LAYOUT_GUBN_1                                                              AS LAYOUT_GUBN_1 \n" +
                "     , A.LAYOUT_GUBN_2                                                              AS LAYOUT_GUBN_2 \n" +
                "     , ''                                                                           AS QR_CODE \n" +
                "     , CONCAT(A.CONTENTS_QTY, A.DISPLAY_UNIT_NAME)                                  AS CONTENTS_QTY_UNIT \n" +
                "     , CASE\n" +
                "          WHEN A.CONTENTS_QTY IS NOT NULL AND A.CONTENTS_QTY > 0 THEN\n" +
                "              CASE\n" +
                "                  WHEN TRIM(A.DISPLAY_UNIT_NAME) = 'ml' THEN ROUND((A.CURR_SAL_PRICE/A.CONTENTS_QTY) * 100) \n" +
                "                  WHEN TRIM(A.DISPLAY_UNIT_NAME) = 'L' THEN ROUND((A.CURR_SAL_PRICE/A.CONTENTS_QTY) * 1000 * 100) \n" +
                "                  ELSE NULL \n" +
                "              END \n" +
                "          ELSE NULL \n" +
                "       END                                                                  \t\tAS COMBINE_DISPLAY_UNIT_PRICE \n" +
                "     , GOOS_INFO_IMG                                                                AS GOOS_INFO_IMG \n" + // 추가속성 목록
                "FROM TETPLU_ESL A \n" +
                "CROSS JOIN (SELECT @rownum := 0) r \n" +
                "WHERE A.STORE_CODE = '14' \n" +
                "ORDER BY A.PLU_CODE ASC;";
        // 쿼리 실행
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8")) {
            for (Map<String, Object> row : results) {
                // String rowString = row.values().stream()
                //         .map(value -> value != null ? value.toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "")
                //         .collect(Collectors.joining(";"));
                String rowString = processData(row);
                writer.write(rowString + "\n");
            }
        }
    }

    private String processData(Map<String,Object> row) {
        // 템플릿 매핑 코드
        String layoutGubn1 = row.get("LAYOUT_GUBN_1") == null ? "" : String.valueOf(row.get("LAYOUT_GUBN_1"));
        String layoutGubn2 = row.get("LAYOUT_GUBN_2") == null ? "" : String.valueOf(row.get("LAYOUT_GUBN_2"));
        row.put("DISPLAY_TYPE", layoutGubn1 + layoutGubn2);
        // 추가속성 처리
        String addPropStr = row.get("GOOS_INFO_IMG") == null ? "" : String.valueOf(row.get("GOOS_INFO_IMG"));
        List<String> addProps = Arrays.asList(addPropStr.split("|"));
        Collections.sort(addProps, Collections.reverseOrder());
        row.put("GOOS_INFO_CD1", addProps.size() > 0 ? addProps.get(0) : "");
        row.put("GOOS_INFO_CD2", addProps.size() > 1 ? addProps.get(1) : "");

        StringBuffer sb = new StringBuffer(400);
        for (String column : columns) {
            Object value = row.get(column);
            String data = value != null ? value.toString().replace("\n", " ").replace("\t", " ").replace(";", " ") : "";
            sb.append(data).append(";");
        }
        return sb.toString();
    }


    public void tetrplEslTest() {
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

            List<Map<String, Object>> tetrplList = new ArrayList<>();

            // 테스트용 리스트 생성
            Map<String, Object> tetrplEsl1 = new HashMap<>();
            tetrplEsl1.put("STORE_CODE", "14");
            tetrplEsl1.put("PLU_CODE", "8804884752367");
            tetrplEsl1.put("PRICE_SECT", "0");
            tetrplEsl1.put("START_DATE", "20240628 ");
            tetrplEsl1.put("END_DATE", "20240628");
            tetrplEsl1.put("MD_CODE", "004510");
            tetrplEsl1.put("PUM_CODE", "0092");
            tetrplEsl1.put("EVENT_SECT", "0");
            tetrplEsl1.put("CLASS_CODE", "95751");
            tetrplEsl1.put("PLU_NAME", "훈제연어슬라이스 딜360g");
            tetrplEsl1.put("CURR_SAL_PRICE", 18500);
            tetrplEsl1.put("GOODS_SECT", "1");
            tetrplEsl1.put("MG_RATE", 25);
            tetrplEsl1.put("TAX_FLAG", "10");
            tetrplEsl1.put("SYS_DATE", "20240628");
            tetrplEsl1.put("SYS_TIME", "092858");
            tetrplEsl1.put("ORIGIN_NAME", "노르웨이");
            tetrplEsl1.put("NOR_SAL_PRICE", 18500);
            tetrplEsl1.put("CONTENTS_QTY", 360);
            tetrplEsl1.put("DISPLAY_UNIT_NAME", "g");
            tetrplEsl1.put("DISPLAY_UNIT_QTY", 360);
            tetrplEsl1.put("DEAL_GUBN", "1");
            tetrplEsl1.put("ORIGIN_CODE", "70");
            tetrplEsl1.put("BRAND_NAME", "선어");
            tetrplEsl1.put("CATE_CODE", "1809000111");
            tetrplEsl1.put("CATE_NAME", "기타수산물가공품");
//            tetrplEsl1.put("MOD_DATE", "");
//            tetrplEsl1.put("MOD_TIME", );
//            tetrplEsl1.put("MOD_EMPNO", );
//            tetrplEsl1.put("IF_DATE", "");
//            tetrplEsl1.put("IF_TIME", "");
//            tetrplEsl1.put("IF_EMPNO", "");
            tetrplEsl1.put("DISPLAY_UNIT_PRICE", 25000);
            tetrplEsl1.put("SELLING_PNT", "천연 향신료로 독트갛ㄴ 향과 맛이 참나무 훈연향과 어우러진 훈제연어 - 수정");
//            tetrplEsl1.put("GOOS_AUTH_IMG", "");
//            tetrplEsl1.put("WINE_SUGR_CONT", "");
//            tetrplEsl1.put("WINE_BODY", "");
//            tetrplEsl1.put("WINE_KIND", "");
//            tetrplEsl1.put("WINE_EVAL_1", "");
//            tetrplEsl1.put("WINE_EVAL_2", "");
//            tetrplEsl1.put("WINE_EVAL_3", "");
//            tetrplEsl1.put("WINE_PLOR", );
//            tetrplEsl1.put("WINE_ITEM_KIND", "");
//            tetrplEsl1.put("WINE_VINT", "");
            tetrplEsl1.put("MOBL_CUP_GOOS_YN", "N");
            tetrplEsl1.put("LAYOUT_GUBN_1", "0001");
            tetrplEsl1.put("LAYOUT_GUBN_2", "0001");
//            tetrplEsl1.put("PLOR_ORIGIN_NAME", );
//            tetrplEsl1.put("QR_CODE", );
            tetrplEsl1.put("CONTENTS_QTY_UNIT", "360g");
//            tetrplEsl1.put("COMBINE_DISPLAY_UNIT", );

            Map<String, Object> tetrplEsl2 = new HashMap<>();
            tetrplEsl2.put("STORE_CODE", "14");
            tetrplEsl2.put("PLU_CODE", "8809150677061");
            tetrplEsl2.put("PRICE_SECT", "0");
            tetrplEsl2.put("START_DATE", "20240628");
            tetrplEsl2.put("END_DATE", "20240628");
            tetrplEsl2.put("MD_CODE", "003741");
            tetrplEsl2.put("PUM_CODE", "0118");
            tetrplEsl2.put("EVENT_SECT", "0");
            tetrplEsl2.put("CLASS_CODE", "99303");
            tetrplEsl2.put("PLU_NAME", "전통육포(번들)");
            tetrplEsl2.put("CURR_SAL_PRICE", 18500);
            tetrplEsl2.put("GOODS_SECT", "1");
            tetrplEsl2.put("MG_RATE", 25);
            tetrplEsl1.put("TAX_FLAG", "10");
            tetrplEsl2.put("SYS_DATE", "20240628");
            tetrplEsl1.put("SYS_TIME", "131656");
            tetrplEsl2.put("ORIGIN_NAME", "미지정");
            tetrplEsl2.put("NOR_SAL_PRICE", 18500);
            tetrplEsl2.put("CONTENTS_QTY", 200);
            tetrplEsl2.put("DISPLAY_UNIT_NAME", "g");
            tetrplEsl2.put("DISPLAY_UNIT_QTY", 200);
            tetrplEsl2.put("DEAL_GUBN", "1");
            tetrplEsl2.put("ORIGIN_CODE", "0");
            tetrplEsl2.put("BRAND_NAME", "홍대감");
            tetrplEsl2.put("CATE_CODE", "1809000226");
            tetrplEsl2.put("CATE_NAME", "육포");
//            tetrplEsl2.put("MOD_DATE", "");
//            tetrplEsl1.put("MOD_TIME", );
//            tetrplEsl1.put("MOD_EMPNO", );
//            tetrplEsl2.put("IF_DATE", "");
//            tetrplEsl2.put("IF_TIME", "");
//            tetrplEsl2.put("IF_EMPNO", "");
            tetrplEsl1.put("DISPLAY_UNIT_PRICE", 21200);
            tetrplEsl2.put("SELLING_PNT", "국내산 우육을 사용하여 구수한 맛이 살아있는 육포 - 수정");
//            tetrplEsl2.put("GOOS_AUTH_IMG", "N");
//            tetrplEsl2.put("WINE_SUGR_CONT", "");
//            tetrplEsl2.put("WINE_BODY", "");
//            tetrplEsl2.put("WINE_KIND", "");
//            tetrplEsl2.put("WINE_EVAL_1", "");
//            tetrplEsl2.put("WINE_EVAL_2", "");
//            tetrplEsl2.put("WINE_EVAL_3", "");
//            tetrplEsl1.put("WINE_PLOR", );
//            tetrplEsl2.put("WINE_ITEM_KIND", "");
//            tetrplEsl2.put("WINE_VINT", "");
            tetrplEsl2.put("MOBL_CUP_GOOS_YN", "N");
            tetrplEsl2.put("LAYOUT_GUBN_1", "0001");
            tetrplEsl2.put("LAYOUT_GUBN_2", "0001");
//            tetrplEsl1.put("PLOR_ORIGIN_NAME", );
//            tetrplEsl1.put("QR_CODE", );
            tetrplEsl2.put("CONTENTS_QTY_UNIT", "200g");
//            tetrplEsl2.put("COMBINE_DISPLAY_UNIT", );

            for (Map<String, Object> tetrplEsl : tetrplList) {
                // 1. MongoDB 저장

                // 2. /articles api 쏘기

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                String baseUrl = "https://10.253.22.112:9002/dashboardservice/common/articles";
                String store = "14";
//            String company = "백화점";
                String company = "%EB%B0%B1%ED%99%94%EC%A0%90";
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
                dataObject.put("STORE_CODE", tetrplEsl.get("STORE_CODE"));
                dataObject.put("PLU_CODE", tetrplEsl.get("PLU_CODE"));
                dataObject.put("PRICE_SECT", tetrplEsl.get("PRICE_SECT"));
                dataObject.put("START_DATE", tetrplEsl.get("START_DATE"));
                dataObject.put("END_DATE", tetrplEsl.get("END_DATE"));
                dataObject.put("MD_CODE", tetrplEsl.get("MD_CODE"));
                dataObject.put("PUM_CODE", tetrplEsl.get("PUM_CODE"));
                dataObject.put("EVENT_SECT", tetrplEsl.get("EVENT_SECT"));
                dataObject.put("CLASS_CODE", tetrplEsl.get("CLASS_CODE"));
                dataObject.put("PLU_NAME", tetrplEsl.get("PLU_NAME"));
                dataObject.put("CURR_SAL_PRICE", tetrplEsl.get("CURR_SAL_PRICE"));
                dataObject.put("GOODS_SECT", tetrplEsl.get("GOODS_SECT"));
                dataObject.put("MG_RATE", tetrplEsl.get("MG_RATE"));
                dataObject.put("TAX_FLAG", tetrplEsl.get("TAX_FLAG"));
                dataObject.put("SYS_DATE", tetrplEsl.get("SYS_DATE"));
                dataObject.put("SYS_TIME", tetrplEsl.get("SYS_TIME"));
                dataObject.put("ORIGIN_NAME", tetrplEsl.get("ORIGIN_NAME"));
                dataObject.put("NOR_SAL_PRICE", tetrplEsl.get("NOR_SAL_PRICE"));
                dataObject.put("CONTENTS_QTY", tetrplEsl.get("CONTENTS_QTY"));
                dataObject.put("DISPLAY_UNIT_NAME", tetrplEsl.get("DISPLAY_UNIT_NAME"));
                dataObject.put("DISPLAY_UNIT_QTY", tetrplEsl.get("DISPLAY_UNIT_QTY"));
                dataObject.put("DEAL_GUBUN", tetrplEsl.get("DEAL_GUBUN"));
                dataObject.put("ORIGIN_CODE", tetrplEsl.get("ORIGIN_CODE"));
                dataObject.put("BRAND_NAME", tetrplEsl.get("BRAND_NAME"));
                dataObject.put("CATE_CODE", tetrplEsl.get("CATE_CODE"));
                dataObject.put("CATE_NAME", tetrplEsl.get("CATE_NAME"));
//                dataObject.put("MOD_DATE", tetrplEsl.get("MOD_DATE"));
//                dataObject.put("MOD_TIME", tetrplEsl.get("MOD_TIME"));
//                dataObject.put("MOD_EMPNO", tetrplEsl.get("MOD_EMPNO"));
//                dataObject.put("IF_DATE", tetrplEsl.get("IF_DATE"));
//                dataObject.put("IF_TIME", tetrplEsl.get("IF_TIME"));
//                dataObject.put("IF_EMPNO", tetrplEsl.get("IF_EMPNO"));
                dataObject.put("DISPLAY_UNIT_PRICE", tetrplEsl.get("DISPLAY_UNIT_PRICE"));
                dataObject.put("SELLNG_PNT", tetrplEsl.get("SELLNG_PNT"));
//                dataObject.put("GOOS_AUTH_IMG", tetrplEsl.get("GOOS_AUTH_IMG"));
//                dataObject.put("WINE_SUGR_CONT", tetrplEsl.get("WINE_SUGR_CONT"));
//                dataObject.put("WINE_BODY", tetrplEsl.get("WINE_BODY"));
//                dataObject.put("WINE_KIND", tetrplEsl.get("WINE_KIND"));
//                dataObject.put("WINE_EVAL_1", tetrplEsl.get("WINE_EVAL_1"));
//                dataObject.put("WINE_EVAL_2", tetrplEsl.get("WINE_EVAL_2"));
//                dataObject.put("WINE_EVAL_3", tetrplEsl.get("WINE_EVAL_3"));
//                dataObject.put("WINE_PLOR", tetrplEsl.get("WINE_PLOR"));
//                dataObject.put("WINE_ITEM_KIND", tetrplEsl.get("WINE_ITEM_KIND"));
//                dataObject.put("WINE_VINT", tetrplEsl.get("WINE_VINT"));
                dataObject.put("MOBL_CUP_GOOS_YN", tetrplEsl.get("MOBL_CUP_GOOS_YN"));
                dataObject.put("LAYOUT_GUBN_1", tetrplEsl.get("LAYOUT_GUBN_1"));
                dataObject.put("LAYOUT_GUBN_2", tetrplEsl.get("LAYOUT_GUBN_2"));
//                dataObject.put("PLOR_ORIGIN_NAME", );
//                dataObject.put("QR_CODE", );
                dataObject.put("CONTENTS_QTY_UNIT", tetrplEsl.get("CONTENTS_QTY_UNIT"));
//                dataObject.put("COMBINE_DISPLAY_UNIT_PRICE", );

                jsonObject.put("articleId", tetrplEsl.get("PLU_CODE"));
                jsonObject.put("articleName", "");
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

                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String responseCodeStr = jsonResponse.getString("responseCode");
                    String responseMessage = jsonResponse.getString("responseMessage");
                    String customBatchId = jsonResponse.isNull("customBatchId") ? null : jsonResponse.getString("customBatchId");

//                    // AIMS_Interface_Log 컬렉션에 insert
//                    AimsInterfaceLog interfaceLog = AimsInterfaceLog.builder()
//                            .responseCode(responseCodeStr)
//                            .responseMessage(responseMessage)
//                            .customBatchId(customBatchId)
//                            .build();
//
//                    aimsInterfaceLogRepository.save(interfaceLog);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                int responseCode = httpConn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 성공적인 응답 처리
                } else {
                    // 에러 처리
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // NoSuchAlgorithmException 예외 처리
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // KeyManagementException 예외 처리
            e.printStackTrace();
        }
    }


}

