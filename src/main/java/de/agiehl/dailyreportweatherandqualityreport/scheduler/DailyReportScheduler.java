package de.agiehl.dailyreportweatherandqualityreport.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyReportScheduler {

    private final DailyReportSender dailyReportSender;

    @Scheduled(cron = "${scheduler.cron}", zone = "${scheduler.timezone}")
    public void sendDailyReport() throws Exception {
        log.info("Triggering daily report sender");
        dailyReportSender.sendDailyReport();
    }
}
