package de.agiehl.dailyreportweatherandqualityreport.scheduler;

import de.agiehl.dailyreportweatherandqualityreport.config.AppProperties;
import de.agiehl.dailyreportweatherandqualityreport.config.WeatherProperties;
import de.agiehl.dailyreportweatherandqualityreport.report.WeeklySummary;
import de.agiehl.dailyreportweatherandqualityreport.report.WeeklySummaryFormatter;
import de.agiehl.dailyreportweatherandqualityreport.report.WeeklySummaryService;
import de.agiehl.dailyreportweatherandqualityreport.telegram.TelegramClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklySummaryScheduler {

    private final WeeklySummaryService weeklySummaryService;
    private final WeeklySummaryFormatter weeklySummaryFormatter;
    private final TelegramClient telegramClient;
    private final AppProperties appProperties;
    private final WeatherProperties weatherProperties;

    @Scheduled(cron = "${scheduler.weekly-cron}", zone = "${scheduler.timezone}")
    public void sendWeeklySummary() {
        log.info("Starting weekly summary generation");
        try {
            LocalDate today = LocalDate.now(ZoneId.of(weatherProperties.timezone()));
            WeeklySummary summary = weeklySummaryService.buildForWeekOf(today);
            String message = weeklySummaryFormatter.format(summary, appProperties.baseUrl() + "/");
            telegramClient.sendMessage(message);
            log.info("Weekly summary sent successfully for week {} – {}", summary.weekStart(), summary.weekEnd());
        } catch (Exception e) {
            log.error("Failed to send weekly summary", e);
        }
    }
}
