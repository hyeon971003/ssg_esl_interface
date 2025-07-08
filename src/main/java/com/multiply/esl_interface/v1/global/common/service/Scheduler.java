package com.multiply.esl_interface.v1.global.common.service;

import com.multiply.esl_interface.v1.web.service.EslInterfaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {

    private final EslInterfaceService eslInterfaceService;

    private static String STATE_TETPLU = null;

    @Scheduled(cron = "${server.schedule.receiveTetpluEsl.cron}") // 매일 6시 10분
    public void receiveVTetpluEslSchedule() {
//        if (STATE_TETPLU != null) return;
//z
//        STATE_TETPLU = "1";
//        eslInterfaceService.receiveTetpluEslEtl();
//        STATE_TETPLU = null;
    }

    @Scheduled(cron = "${server.schedule.receiveVTetrplEsl.cron}") // 0,30
    public void receiveVTetrplEslSchedule() {
        //eslInterfaceService.receiveVTetrplEslEtl();
    }

}
