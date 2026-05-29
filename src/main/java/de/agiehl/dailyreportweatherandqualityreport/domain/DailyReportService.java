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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailyReportService {

    private static final int HOURS_PER_DAY = 24;

    private final DailyReportRepository repository;
    private final HourlyReadingRepository hourlyReadingRepository;
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

        PollenApiResponse.Hourly pollenHourly = pollen.hourly();
        if (pollenHourly != null) {
            report.setAlderPollen(maxDouble(pollenHourly.alderPollen()));
            report.setBirchPollen(maxDouble(pollenHourly.birchPollen()));
            report.setGrassPollen(maxDouble(pollenHourly.grassPollen()));
            report.setMugwortPollen(maxDouble(pollenHourly.mugwortPollen()));
            report.setOlivePollen(maxDouble(pollenHourly.olivePollen()));
            report.setRagweedPollen(maxDouble(pollenHourly.ragweedPollen()));
            report.setEuropeanAqi(maxInt(pollenHourly.europeanAqi()));
        }

        DailyReport saved = repository.save(report);
        replaceHourlyReadings(saved.getId(), weather.hourly(), pollenHourly);
        return saved;
    }

    public Optional<DailyReport> findById(String id) {
        return repository.findById(id);
    }

    public Optional<DailyReport> findByReportDate(LocalDate date) {
        return repository.findByReportDate(date);
    }

    public List<DailyReport> findAllSortedByDateDesc() {
        return repository.findAllByOrderByReportDateDesc();
    }

    public List<HourlyReading> findHourlyReadings(String dailyReportId) {
        return hourlyReadingRepository.findByDailyReportIdOrderByHourOfDayAsc(dailyReportId);
    }

    private void replaceHourlyReadings(String dailyReportId,
                                       WeatherApiResponse.HourlyWeather weatherHourly,
                                       PollenApiResponse.Hourly pollenHourly) {
        hourlyReadingRepository.deleteByDailyReportId(dailyReportId);

        List<HourlyReading> readings = new ArrayList<>();
        for (int hour = 0; hour < HOURS_PER_DAY; hour++) {
            HourlyReading reading = buildHourlyReading(dailyReportId, hour, weatherHourly, pollenHourly);
            if (hasAnyValue(reading)) {
                readings.add(reading);
            }
        }
        hourlyReadingRepository.saveAll(readings);
    }

    private HourlyReading buildHourlyReading(String dailyReportId, int hour,
                                             WeatherApiResponse.HourlyWeather weatherHourly,
                                             PollenApiResponse.Hourly pollenHourly) {
        HourlyReading reading = new HourlyReading();
        reading.setId(UUID.randomUUID().toString());
        reading.setNew(true);
        reading.setDailyReportId(dailyReportId);
        reading.setHourOfDay(hour);
        reading.setTemperature(weatherHourly != null ? doubleAt(weatherHourly.temperature(), hour) : null);
        if (pollenHourly != null) {
            reading.setAlderPollen(doubleAt(pollenHourly.alderPollen(), hour));
            reading.setBirchPollen(doubleAt(pollenHourly.birchPollen(), hour));
            reading.setGrassPollen(doubleAt(pollenHourly.grassPollen(), hour));
            reading.setMugwortPollen(doubleAt(pollenHourly.mugwortPollen(), hour));
            reading.setOlivePollen(doubleAt(pollenHourly.olivePollen(), hour));
            reading.setRagweedPollen(doubleAt(pollenHourly.ragweedPollen(), hour));
        }
        return reading;
    }

    private boolean hasAnyValue(HourlyReading reading) {
        return reading.getTemperature() != null
                || reading.getAlderPollen() != null
                || reading.getBirchPollen() != null
                || reading.getGrassPollen() != null
                || reading.getMugwortPollen() != null
                || reading.getOlivePollen() != null
                || reading.getRagweedPollen() != null;
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

    private Double doubleAt(List<Double> list, int index) {
        return (list != null && index < list.size()) ? list.get(index) : null;
    }

    private Double maxDouble(List<Double> list) {
        if (list == null) return null;
        return list.stream()
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
    }

    private Integer maxInt(List<Integer> list) {
        if (list == null) return null;
        return list.stream()
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private String extractTime(String isoDateTime) {
        if (isoDateTime == null || !isoDateTime.contains("T")) return null;
        return isoDateTime.substring(isoDateTime.indexOf("T") + 1);
    }
}
