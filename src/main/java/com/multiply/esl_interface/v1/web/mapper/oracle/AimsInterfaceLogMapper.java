package com.multiply.esl_interface.v1.web.mapper.oracle;

import com.multiply.esl_interface.v1.web.model.*;
import org.apache.ibatis.annotations.Mapper;

@Mapper()
public interface AimsInterfaceLogMapper {
    void save(AimsInterfaceLog aimsInterfaceLog);
}
