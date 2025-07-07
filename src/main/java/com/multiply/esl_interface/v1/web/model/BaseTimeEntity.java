package com.multiply.esl_interface.v1.web.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Data
@Getter
@NoArgsConstructor
public abstract class BaseTimeEntity {

    @CreatedDate
    private Instant regDt;

    @LastModifiedDate
    private Instant modDt;

}
