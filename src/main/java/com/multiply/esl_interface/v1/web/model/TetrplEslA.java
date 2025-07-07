package com.multiply.esl_interface.v1.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
@Schema(description = "V_TETRPL_ESL(AIMS insert용 오라클 수신) 모델")
public class TetrplEslA {

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
    private Integer currSalPrice;
    private String goodsSect;
    private Double mgRate;
    private String taxFlag;
    private String sysDate;
    private String sysTime;
    private String originName;
    private Integer norSalPrice;
    private Integer contentsQty;
    private String displayUnitName;
    private Integer displayUnitQty;
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
    private Long displayUnitPrice;
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
    private String contentsQtyUnit;
    private String qrCode;
    private Long combineDisplayUnitPrice;
    private String goosInfoImg;
}
