package de.agiehl.dailyreportweatherandqualityreport.domain;

import de.agiehl.dailyreportweatherandqualityreport.config.WeatherProperties;
import de.agiehl.dailyreportweatherandqualityreport.pollen.model.PollenApiResponse;
import de.agiehl.dailyreportweatherandqualityreport.weather.model.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final DailyReportRepository repository;
    private final WeatherProperties weatherProperties;

    @Transactional
    public DailyReport findOrCreateForToday() {
        LocalDate today = LocalDate.now(ZoneId.of(weatherProperties.timezone()));
        return repository.findByReportDate(today)
                .orElseGet(() -> createEmptyReport(today));
    }

    @Transactional
    public DailyReport updateWeatherAndPollen(String id, WeatherApiResponse weather, PollenApiResponse pollen) {
        DailyReport report = repository.findById(id).orElseThrow();

        WeatherApiResponse.DailyWeather daily = weather.daily();
        report.setWeatherCode(firstInt(daily.weathercode()));
        report.setTemperatureMax(firstDouble(daily.temperatureMax()));
        report.setTemperatureMin(firstDouble(daily.temperatureMin()));
        report.setPrecipitationSum(firstDouble(daily.precipitationSum()));
        report.setPrecipitationProbability(firstInt(daily.precipitationProbability()));
        report.setWindspeedMax(firstDouble(daily.windspeedMax()));
        report.setUvIndexMax(firstDouble(daily.uvIndexMax()));
        report.setSunrise(extractTime(firstStr(daily.sunrise())));
        report.setSunset(extractTime(firstStr(daily.sunset())));

        PollenApiResponse.DailyPollen pollen0 = pollen.daily();
        if (pollen0 != null) {
            report.setAlderPollen(firstDouble(pollen0.alderPollen()));
            report.setBirchPollen(firstDouble(pollen0.birchPollen()));
            report.setGrassPollen(firstDouble(pollen0.grassPollen()));
            report.setMugwortPollen(firstDouble(pollen0.mugwortPollen()));
            report.setOlivePollen(firstDouble(pollen0.olivePollen()));
            report.setRagweedPollen(firstDouble(pollen0.ragweedPollen()));
        }

        return repository.save(report);
    }

    public Optional<DailyReport> findById(String id) {
        return repository.findById(id);
    }

    public List<DailyReport> findAllSortedByDateDesc() {
        return repository.findAllByOrderByReportDateDesc();
    }

    private DailyReport createEmptyReport(LocalDate date) {
        DailyReport report = new DailyReport();
        report.setId(UUID.randomUUID().toString());
        report.setNew(true);
        report.setReportDate(date);
        report.setCreatedAt(LocalDateTime.now());
        return repository.save(report);
    }

    private Double firstDouble(List<Double> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    private Integer firstInt(List<Integer> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    private String firstStr(List<String> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    private String extractTime(String isoDateTime) {
        if (isoDateTime == null || !isoDateTime.contains("T")) return null;
        return isoDateTime.substring(isoDateTime.indexOf("T") + 1);
    }
}
