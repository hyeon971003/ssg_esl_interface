package com.multiply.esl_interface.v1.global.common.converter;

import lombok.extern.log4j.Log4j2;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

@Log4j2
public class EncodingConverter {

    // UTF-8 -> US7ASCII
    public static String utf8ToUs7ascii(String str) {
        try {
            if(str != null && str != "") {
                return new String(str.getBytes("EUC-KR"), "8859_1");
            }
        } catch (UnsupportedEncodingException ue) {
            throw new RuntimeException(ue);
        }

        return str;
    }

    public static String bytesToString(String byteStr) {
        if (byteStr == null || byteStr.length() == 0) {
            return "";
        }
        String returnStr = "";

        try {
            byte[] bytes = new BigInteger(byteStr, 16).toByteArray();
            returnStr = new String(bytes, "EUC-KR").trim();
        } catch (Exception ue) {
            // throw new RuntimeException(ue);
            log.error("byteToString error : {} -> {}", byteStr, ue.getMessage());
            returnStr = byteStr;
        }
        return returnStr;
    }

    // US7ASCII -> UTF-8
    public static String us7asciiToUtf8(String str) {
        try {
            if(str != null && str != "") {
                return new String(str.getBytes("8859_1"), "EUC-KR");
            }
        } catch (UnsupportedEncodingException ue) {
            throw new RuntimeException(ue);
        }

        return str;
    }
}
