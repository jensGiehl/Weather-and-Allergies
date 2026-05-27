package de.agiehl.dailyreportweatherandqualityreport.scheduler;

import de.agiehl.dailyreportweatherandqualityreport.config.AppProperties;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReport;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReportService;
import de.agiehl.dailyreportweatherandqualityreport.pollen.PollenClient;
import de.agiehl.dailyreportweatherandqualityreport.pollen.model.PollenApiResponse;
import de.agiehl.dailyreportweatherandqualityreport.report.ReportFormatter;
import de.agiehl.dailyreportweatherandqualityreport.telegram.TelegramClient;
import de.agiehl.dailyreportweatherandqualityreport.weather.WeatherClient;
import de.agiehl.dailyreportweatherandqualityreport.weather.model.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyReportScheduler {

    private final WeatherClient weatherClient;
    private final PollenClient pollenClient;
    private final DailyReportService dailyReportService;
    private final ReportFormatter reportFormatter;
    private final TelegramClient telegramClient;
    private final AppProperties appProperties;

    @Scheduled(cron = "${scheduler.cron}", zone = "${scheduler.timezone}")
    public void sendDailyReport() {
        log.info("Starting daily report generation");
        try {
            DailyReport report = dailyReportService.findOrCreateForToday();
            WeatherApiResponse weather = weatherClient.fetchTodayWeather();
            PollenApiResponse pollen = pollenClient.fetchTodayPollen();
            dailyReportService.updateWeatherAndPollen(report.getId(), weather, pollen);

            String reportLink = appProperties.baseUrl() + "/report/" + report.getId();
            String message = reportFormatter.format(weather, pollen, reportLink);
            telegramClient.sendMessage(message);
            log.info("Daily report sent successfully, id={}", report.getId());
        } catch (Exception e) {
            log.error("Failed to send daily report", e);
        }
    }
}
