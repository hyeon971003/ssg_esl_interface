package com.multiply.esl_interface.v1.web.mapper.aimscore;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AimsCoreMapper {

    /* station-code 목록 */
    List<String> selectStationCodes(String order);

}
