package com.multiply.esl_interface.v1.web.model;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "aims_interface_log")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AimsInterfaceLog {

    @Id
    private String id;
    private String ifType;
    private String ifDate;
    private String ifTime;
    private String ifResult;
    private Integer ifTotalCnt;
    private Integer ifSuccessCnt;
    private Integer ifFailCnt;
    private List<String> failedPluCodes;

}
