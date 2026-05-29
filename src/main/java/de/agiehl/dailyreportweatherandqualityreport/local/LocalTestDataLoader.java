package de.agiehl.dailyreportweatherandqualityreport.local;

import de.agiehl.dailyreportweatherandqualityreport.domain.AllergyEntryService;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReport;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Profile("local")
@RequiredArgsConstructor
class LocalTestDataLoader implements ApplicationRunner {

    private final DailyReportRepository reportRepository;
    private final AllergyEntryService allergyEntryService;

    @Override
    public void run(ApplicationArguments args) {
        LocalDate today = LocalDate.now();

        Optional<String> id1 = seedReportIfAbsent(today.minusDays(1), 3, 25.1, 16.4, 0.0, 10, 12.0, 7.8, "06:13", "21:02", 5.0, 20.0, 15.0, 0.0, 0.0, 0.0);
        Optional<String> id2 = seedReportIfAbsent(today.minusDays(2), 80, 18.5, 12.0, 12.4, 85, 25.0, 1.1, "06:14", "21:01", 8.0, 30.0, 22.0, 2.0, 0.0, 0.0);
        Optional<String> id3 = seedReportIfAbsent(today.minusDays(3), 2, 20.0, 11.5, 0.0, 5, 8.0, 6.5, "06:15", "21:00", 3.0, 10.0, 5.0, 0.0, 0.0, 0.0);
        seedReportIfAbsent(today.minusDays(5), 45, 16.2, 9.8, 3.0, 40, 20.0, 4.0, "06:17", "20:58", 20.0, 60.0, 30.0, 5.0, 0.0, 1.0);
        Optional<String> id5 = seedReportIfAbsent(today.minusDays(7), 71, 12.8, 6.1, 8.6, 70, 30.0, 2.5, "06:20", "20:55", 0.0, 5.0, 12.0, 0.0, 0.0, 0.0);

        id1.ifPresent(id -> allergyEntryService.save(id, "Mama",  List.of("TRAENENDE_AUGEN", "MUEDIGKEIT")));
        id2.ifPresent(id -> allergyEntryService.save(id, "Papa",  List.of("HUSTENREIZ", "ATEMNOT")));
        id2.ifPresent(id -> allergyEntryService.save(id, "Mama",  List.of("NIESEN", "VERSTOPFTE_NASE", "KOPFSCHMERZEN")));
        id3.ifPresent(id -> allergyEntryService.save(id, "Laura", List.of("HAUTAUSSCHLAG")));
        id5.ifPresent(id -> allergyEntryService.save(id, "Papa",  List.of("MUEDIGKEIT", "HALSSCHMERZEN")));
        id5.ifPresent(id -> allergyEntryService.save(id, "Jonas", List.of("NIESEN", "JUCKENDE_AUGEN", "KRIBBELN_HAUT")));
    }

    private Optional<String> seedReportIfAbsent(LocalDate date, int weatherCode,
                                                double tempMax, double tempMin,
                                                double precipSum, int precipProb, double windMax, double uvMax,
                                                String sunrise, String sunset,
                                                double alder, double birch, double grass,
                                                double mugwort, double olive, double ragweed) {
        if (reportRepository.findByReportDate(date).isPresent()) {
            return Optional.empty();
        }
        DailyReport report = new DailyReport();
        report.setId(UUID.randomUUID().toString());
        report.setReportDate(date);
        report.setWeatherCode(weatherCode);
        report.setTemperatureMax(tempMax);
        report.setTemperatureMin(tempMin);
        report.setPrecipitationSum(precipSum);
        report.setPrecipitationProbability(precipProb);
        report.setWindspeedMax(windMax);
        report.setUvIndexMax(uvMax);
        report.setSunrise(sunrise);
        report.setSunset(sunset);
        report.setAlderPollen(alder);
        report.setBirchPollen(birch);
        report.setGrassPollen(grass);
        report.setMugwortPollen(mugwort);
        report.setOlivePollen(olive);
        report.setRagweedPollen(ragweed);
        report.setCreatedAt(LocalDateTime.now());
        report.setNew(true);
        reportRepository.save(report);
        return Optional.of(report.getId());
    }
}
