package com.multiply.esl_interface.v1.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@Builder
@Data
@Table(name = "tetrpl_esl")
@Schema(description = "V_TETRPL_ESL(몽고 insert용 오라클 수신) 모델")
@Getter
@Setter
@Entity
public class TetrplEsl {
    public TetrplEsl() {
        // 기본 생성자
    }

    private String storeCode;
    private String mntDate;
    private String mntTime;
    @Id
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
    private String mntEmpno;
    private String sysDate;
    private String sysTime;
    private String originName;
    private Integer norSalPrice;
    private Integer contentsQty;
    private String displayUnitName;
    private Integer displayUnitQty;
    private String brandName;
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
    private String goosInfoImg;
}
