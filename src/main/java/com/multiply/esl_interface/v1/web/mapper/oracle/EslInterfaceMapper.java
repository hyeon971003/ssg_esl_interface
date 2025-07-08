package com.multiply.esl_interface.v1.web.mapper.oracle;

import com.multiply.esl_interface.v1.web.model.PagingRequest;
import com.multiply.esl_interface.v1.web.model.TetpluEsl;
import com.multiply.esl_interface.v1.web.model.TetrplEsl;
import com.multiply.esl_interface.v1.web.model.TetrplEslA;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper()
public interface EslInterfaceMapper {

    /**
     * 일반 테이블 조회(몽고 insert용)
     * @param pagingRequest 페이징 정보
     */
    List<TetpluEsl> selectVTetpluEsl(PagingRequest pagingRequest);

    /**
     * 긴급 테이블 조회(몽고 insert용)
     */
    List<TetrplEsl> selectVTetrplEsl();

    /**
     * 긴급 테이블 조회(AIMS insert용)
     */
    List<TetrplEslA> selectVTetrplEslA();
    List<TetpluEsl> selectVTetrplEslTest();

}
