package com.multiply.esl_interface.v1.web.mapper;

import com.multiply.esl_interface.v1.web.model.VTetpluEsl;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface OracleMapper {

    List<VTetpluEsl> selectVTetpluEslList();

    Map<String, Object> selectVTetrplEslList();

}
