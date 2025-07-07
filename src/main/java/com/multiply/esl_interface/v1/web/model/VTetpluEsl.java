package com.multiply.esl_interface.v1.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@Schema(description = "V_TETPLU_ESL(오라클 수신용) 모델")
public class VTetpluEsl {

    private String storeCode;
    private String pluCode;
    private String priceSect;
    private String startDate;
    private String endDate;
    private String mdCode;
    private String pumCode;
    private String eventSect;
    private String classCode;
    private String pluName;
    private int currSalPrice;
    private String goodsSect;
    private Double mgRate;
    private String taxFlag;
    private String sysDate;
    private String sysTime;
    private String originName;
    private int norSalPrice;
    private int contentsQty;
    private String displayUnitName;
    private int displayUnitQty;
    private String dealGubn;
    private String originCode;
    private String brandName;
    private String cateCode;
    private String cateName;
    private String modDate;
    private String modTime;
    private String modEmpno;
    private String ifDate;
    private String ifTime;
    private String ifEmpno;
    private long displayUnitPrice;
    private String sellngPnt;
    private String goosAuthImg;
    private String wineSugrCont;
    private String wineBody;
    private String wineKind;
    private String wineEval1;
    private String wineEval2;
    private String wineEval3;
    private String winePlor;
    private String wineItemKind;
    private String wineVint;
    private String moblCupGoosYn;
    private String layoutGubn1;
    private String layoutGubn2;
    private String plorOriginName;
    private String qrCode;
    private String contentsQtyUnit;
    private int combineDisplayUnitPrice;

}
