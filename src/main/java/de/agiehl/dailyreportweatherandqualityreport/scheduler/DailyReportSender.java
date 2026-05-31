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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportSender {

    private final WeatherClient weatherClient;
    private final PollenClient pollenClient;
    private final DailyReportService dailyReportService;
    private final ReportFormatter reportFormatter;
    private final TelegramClient telegramClient;
    private final AppProperties appProperties;

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 300000))
    public void sendDailyReport() throws Exception {
        log.info("Starting daily report generation (retryable)");

        DailyReport report = dailyReportService.findOrCreateForToday();
        WeatherApiResponse weather = weatherClient.fetchTodayWeather();
        PollenApiResponse pollen = pollenClient.fetchTodayPollen();
        dailyReportService.updateWeatherAndPollen(report.getId(), weather, pollen);

        String reportLink = appProperties.baseUrl() + "/report/" + report.getId();
        String message = reportFormatter.format(weather, pollen, reportLink);
        telegramClient.sendMessage(message);
        log.info("Daily report sent successfully, id={}", report.getId());
    }

    @Recover
    public void recover(Exception e) {
        log.error("Exhausted retries: failed to send daily report", e);
    }
}

