package com.multiply.esl_interface.v1.web.controller;

import com.multiply.esl_interface.v1.web.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class QueryController {

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadQueryResult() throws IOException {
        // 현재 날짜와 시간을 yyyyMMdd_HHmmss 형식으로 포맷
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String outputFilePath = "/esl_if/daily/" + formattedDateTime + ".dat";
        databaseService.executeQueryToFile(outputFilePath);  // 매개변수 제거 후 호출

        File file = new File(outputFilePath);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", file.getName());
        headers.setContentLength(file.length());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}