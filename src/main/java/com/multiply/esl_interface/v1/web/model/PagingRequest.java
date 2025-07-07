package com.multiply.esl_interface.v1.web.model;

import lombok.*;

@AllArgsConstructor
@Builder
@Data
public class PagingRequest {
    long startNum;
    long endNum;
    long count;
}
