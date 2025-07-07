package com.multiply.esl_interface.v1.global.common.service;

import com.multiply.esl_interface.v1.global.common.dto.CSVTetpluEslResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CSVService {

    public List<CSVTetpluEslResponse> parseCSV(InputStream inputStream) {
        List<CSVTetpluEslResponse> myModelList = new ArrayList<>();
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                CSVTetpluEslResponse myModel = new CSVTetpluEslResponse(
                        csvRecord.get("Column1"),
                        csvRecord.get("Column2"),
                        Integer.parseInt(csvRecord.get("Column3"))
                );
                myModelList.add(myModel);
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV file parsing error: " + e.getMessage());
        }
        return myModelList;
    }

}
