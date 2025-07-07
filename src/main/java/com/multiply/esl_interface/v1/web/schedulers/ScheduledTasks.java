package com.multiply.esl_interface.v1.web.schedulers;

import com.multiply.esl_interface.v1.web.service.DatabaseService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class ScheduledTasks {

    private final DatabaseService databaseService;

    // 생성자 주입 방식?
    public ScheduledTasks(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    //@Scheduled(cron = "0 0 6 * * *") // 매일 06시에 실행
    public void performTask() {
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String outputFileName = "esl_if/daily/" + formattedDate + ".dat";

        try {
            databaseService.executeQueryToFile(outputFileName);
        } catch (IOException e) {
            e.printStackTrace(); // TODO 예외 처리
        }
    }

    //@Scheduled(cron = "0 30 * * * *") // 매 30분 단위로 수행되는지 테스트
    public void tetrplEslTestScheduler() {
        databaseService.tetrplEslTest();
    }
}
