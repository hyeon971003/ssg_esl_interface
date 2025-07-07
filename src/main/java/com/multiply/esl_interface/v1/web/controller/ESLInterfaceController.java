package com.multiply.esl_interface.v1.web.controller;

import com.multiply.esl_interface.v1.global.common.dto.ApiResponse;
import com.multiply.esl_interface.v1.web.service.EslInterfaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
@RestController
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1")
public class ESLInterfaceController {

    private final EslInterfaceService eslInterfaceService;

    /*************************************************************
     * @title
     * @description
     * @param
     * @return
     *************************************************************/
    @Parameter(name = "test", hidden = true)
    @Operation(summary = "테스트", description = "Hellow world")
    @SecurityRequirement(name = "Basic")
    @GetMapping(value = "/test")
    public ResponseEntity<ApiResponse> check(){
        return ResponseEntity.ok().body(null);
    }

    /*************************************************************
     * @title
     * @description
     * @param
     * @return
     *************************************************************/
    @Parameter(name = "", hidden = true)
    @Operation(summary = "AIMS 통신 테스트", description = "AIMS 통신 테스트")
    @SecurityRequirement(name = "Basic")
    @PostMapping(value = "/aims")
    public ResponseEntity<String> sendPostRequestTest() throws NoSuchAlgorithmException {
        return ResponseEntity.ok().body(null);
    }

    /*************************************************************
     * @title
     * @description
     * @param
     * @return
     *************************************************************/
    @Parameter(name = "", hidden = true)
    @Operation(summary = "AIMS 일반 데이터 업로드 테스트", description = "AIMS 일반 데이터 업로드 테스트")
    @SecurityRequirement(name = "Basic")
    @PostMapping(value = "/test/plu")
    public ResponseEntity<String> sendTetpluEslTest() throws NoSuchAlgorithmException {
        eslInterfaceService.receiveTetpluEsl();
        return ResponseEntity.ok().body(null);
    }

    /*************************************************************
     * @title
     * @description
     * @param
     * @return
     *************************************************************/
    @Parameter(name = "", hidden = true)
    @Operation(summary = "AIMS 긴급 데이터 업로드 테스트", description = "AIMS 긴급 데이터 업로드 테스트")
    @SecurityRequirement(name = "Basic")
    @PostMapping(value = "/test/rpl")
    public ResponseEntity<String> sendTetrplEslTest() throws NoSuchAlgorithmException {
        eslInterfaceService.receiveVTetrplEsl();
        return ResponseEntity.ok().body(null);
    }

}
