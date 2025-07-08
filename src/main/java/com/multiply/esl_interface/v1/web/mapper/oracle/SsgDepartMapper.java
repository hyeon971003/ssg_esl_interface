package com.multiply.esl_interface.v1.web.mapper.oracle;

import com.multiply.esl_interface.v1.web.model.TetpluEsl;
import com.multiply.esl_interface.v1.web.model.TetrplEslA;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper()
public interface SsgDepartMapper {

    /**
     * 일반 테이블 전체 조회
     * @param paramMap 페이징 정보
     */
    List<TetpluEsl> selectVTetpluEslAll(Map paramMap);

    /**
     * 일반 테이블의 당일 데이터 조회
     * @param paramMap 페이징 정보
     */
    List<TetpluEsl> selectVTetpluEsl(Map paramMap);

    /**
     * 긴급 테이블 조회
     */
    List<TetrplEslA> selectVTetrplEslA(Map paramMap);

}
