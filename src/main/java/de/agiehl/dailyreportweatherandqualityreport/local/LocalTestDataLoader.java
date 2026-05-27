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

        String id0 = seedReport(today,          61, 22.3, 14.1,  5.2, 60, 18.0, 5.2, "06:12", "21:03", 12.0, 45.0,  8.0, 0.0, 0.0, 0.0);
        String id1 = seedReport(today.minusDays(1),  3, 25.1, 16.4,  0.0, 10, 12.0, 7.8, "06:13", "21:02",  5.0, 20.0, 15.0, 0.0, 0.0, 0.0);
        String id2 = seedReport(today.minusDays(2), 80, 18.5, 12.0, 12.4, 85, 25.0, 1.1, "06:14", "21:01",  8.0, 30.0, 22.0, 2.0, 0.0, 0.0);
        String id3 = seedReport(today.minusDays(3),  2, 20.0, 11.5,  0.0,  5,  8.0, 6.5, "06:15", "21:00",  3.0, 10.0,  5.0, 0.0, 0.0, 0.0);
                     seedReport(today.minusDays(5), 45, 16.2,  9.8,  3.0, 40, 20.0, 4.0, "06:17", "20:58", 20.0, 60.0, 30.0, 5.0, 0.0, 1.0);
        String id5 = seedReport(today.minusDays(7), 71, 12.8,  6.1,  8.6, 70, 30.0, 2.5, "06:20", "20:55",  0.0,  5.0, 12.0, 0.0, 0.0, 0.0);

        allergyEntryService.save(id0, "Papa",   List.of("NIESEN", "JUCKENDE_AUGEN"));
        allergyEntryService.save(id0, "Kind 1", List.of("NIESEN", "LAUFENDE_NASE"));
        allergyEntryService.save(id1, "Mama",   List.of("TRAENENDE_AUGEN", "MUEDIGKEIT"));
        allergyEntryService.save(id2, "Papa",   List.of("HUSTENREIZ", "ATEMNOT"));
        allergyEntryService.save(id2, "Mama",   List.of("NIESEN", "VERSTOPFTE_NASE", "KOPFSCHMERZEN"));
        allergyEntryService.save(id3, "Kind 2", List.of("HAUTAUSSCHLAG"));
        allergyEntryService.save(id5, "Papa",   List.of("MUEDIGKEIT", "HALSSCHMERZEN"));
        allergyEntryService.save(id5, "Kind 1", List.of("NIESEN", "JUCKENDE_AUGEN", "KRIBBELN_HAUT"));
    }

    private String seedReport(LocalDate date, int weatherCode,
                               double tempMax, double tempMin,
                               double precipSum, int precipProb, double windMax, double uvMax,
                               String sunrise, String sunset,
                               double alder, double birch, double grass,
                               double mugwort, double olive, double ragweed) {
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
        return report.getId();
    }
}
