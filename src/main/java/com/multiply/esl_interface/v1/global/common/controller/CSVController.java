package com.multiply.esl_interface.v1.global.common.controller;

import com.multiply.esl_interface.v1.global.common.dto.CSVTetpluEslResponse;
import com.multiply.esl_interface.v1.global.common.service.CSVService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/csv")
public class CSVController {


    private CSVService service;

    /*************************************************************
     * @title
     * @description
     * @param
     * @return
     *************************************************************/
    @Parameter(name = "", hidden = true)
    @Operation(summary = "AIMS 통신 테스트", description = "AIMS 통신 테스트")
    public ResponseEntity<List<CSVTetpluEslResponse>> csvUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            List<CSVTetpluEslResponse> response = service.parseCSV(file.getInputStream());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
