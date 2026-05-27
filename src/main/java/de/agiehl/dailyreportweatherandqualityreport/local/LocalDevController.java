package de.agiehl.dailyreportweatherandqualityreport.local;

import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReport;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReportService;
import de.agiehl.dailyreportweatherandqualityreport.pollen.PollenClient;
import de.agiehl.dailyreportweatherandqualityreport.weather.WeatherClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@Profile("local")
@RequiredArgsConstructor
@RequestMapping("/dev")
class LocalDevController {

    private final DailyReportService dailyReportService;
    private final WeatherClient weatherClient;
    private final PollenClient pollenClient;

    @GetMapping
    public String devPage() {
        return "dev";
    }

    @PostMapping("/trigger")
    @ResponseBody
    public DevTriggerResult triggerDailyReport() throws IOException, InterruptedException {
        DailyReport report = dailyReportService.findOrCreateForToday();
        DailyReport updated = dailyReportService.updateWeatherAndPollen(
                report.getId(),
                weatherClient.fetchTodayWeather(),
                pollenClient.fetchTodayPollen()
        );
        return DevTriggerResult.from(updated);
    }
}
