package com.multiply.esl_interface.v1.web.model;

import com.multiply.esl_interface.v1.global.common.converter.StringListConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
@AllArgsConstructor
@Builder
@Data
@Schema(description = "aims 저장 로그")
public class AimsInterfaceLog {
    private String id;
    private String ifType;
    private String ifDate;
    private String ifTime;
    private String ifResult;
    private Integer ifTotalCnt;
    private Integer ifSuccessCnt;
    private Integer ifFailCnt;
    private String failedPluCodes;

}
