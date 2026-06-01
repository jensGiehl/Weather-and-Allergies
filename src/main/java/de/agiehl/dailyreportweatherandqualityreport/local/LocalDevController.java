package de.agiehl.dailyreportweatherandqualityreport.local;

import de.agiehl.dailyreportweatherandqualityreport.config.AppProperties;
import de.agiehl.dailyreportweatherandqualityreport.config.WeatherProperties;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReport;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReportService;
import de.agiehl.dailyreportweatherandqualityreport.pollen.PollenClient;
import de.agiehl.dailyreportweatherandqualityreport.pollen.model.PollenApiResponse;
import de.agiehl.dailyreportweatherandqualityreport.report.ReportFormatter;
import de.agiehl.dailyreportweatherandqualityreport.report.WeeklySummary;
import de.agiehl.dailyreportweatherandqualityreport.report.WeeklySummaryFormatter;
import de.agiehl.dailyreportweatherandqualityreport.report.WeeklySummaryService;
import de.agiehl.dailyreportweatherandqualityreport.weather.WeatherClient;
import de.agiehl.dailyreportweatherandqualityreport.weather.model.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@Controller
@Profile("local")
@RequiredArgsConstructor
@RequestMapping("/dev")
class LocalDevController {

    private final DailyReportService dailyReportService;
    private final WeatherClient weatherClient;
    private final PollenClient pollenClient;
    private final ReportFormatter reportFormatter;
    private final WeeklySummaryService weeklySummaryService;
    private final WeeklySummaryFormatter weeklySummaryFormatter;
    private final AppProperties appProperties;
    private final WeatherProperties weatherProperties;

    @GetMapping
    public String devPage() {
        return "dev";
    }

    @PostMapping("/trigger")
    @ResponseBody
    public DevTriggerResult triggerDailyReport() throws IOException, InterruptedException {
        DailyReport report = dailyReportService.findOrCreateForToday();
        WeatherApiResponse weather = weatherClient.fetchTodayWeather();
        PollenApiResponse pollen = pollenClient.fetchTodayPollen();
        DailyReport updated = dailyReportService.updateWeatherAndPollen(report.getId(), weather, pollen);

        String reportLink = appProperties.baseUrl() + "/report/" + updated.getId();
        String detailLog = appProperties.baseUrl() + "/day/" + LocalDate.now().format(ISO_DATE);
        String telegramMessage = reportFormatter.format(weather, pollen, reportLink, detailLog);
        return DevTriggerResult.from(updated, telegramMessage);
    }

    @GetMapping("/weekly-preview")
    @ResponseBody
    public DevWeeklyPreviewResult previewWeeklyMessage() {
        LocalDate today = LocalDate.now(ZoneId.of(weatherProperties.timezone()));
        WeeklySummary summary = weeklySummaryService.buildForWeekOf(today);
        String message = weeklySummaryFormatter.format(summary, appProperties.baseUrl() + "/");
        return new DevWeeklyPreviewResult(
                summary.weekStart().toString(),
                summary.weekEnd().toString(),
                message
        );
    }
}
