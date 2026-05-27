package de.agiehl.dailyreportweatherandqualityreport.web;

import de.agiehl.dailyreportweatherandqualityreport.domain.*;
import de.agiehl.dailyreportweatherandqualityreport.report.PollenLevel;
import de.agiehl.dailyreportweatherandqualityreport.report.UvIndexLevel;
import de.agiehl.dailyreportweatherandqualityreport.report.WeatherCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExportController {

    private final DailyReportService dailyReportService;
    private final AllergyEntryService allergyEntryService;
    private final PersonLoader personLoader;
    private final SymptomLoader symptomLoader;

    @GetMapping("/export")
    public List<ExportData> exportAll() {
        return dailyReportService.findAllSortedByDateDesc().stream()
                .map(this::toExportData)
                .toList();
    }

    private ExportData toExportData(DailyReport report) {
        return new ExportData(
                report.getReportDate(),
                toWeather(report),
                toPollenList(report),
                toEntries(report)
        );
    }

    private ExportData.Weather toWeather(DailyReport report) {
        WeatherCondition condition = resolveCondition(report);
        UvIndexLevel uvLevel = UvIndexLevel.fromValue(report.getUvIndexMax());
        return new ExportData.Weather(
                report.getWeatherCode(),
                condition.getLabel(),
                report.getTemperatureMax(),
                report.getTemperatureMin(),
                report.getPrecipitationSum(),
                report.getPrecipitationProbability(),
                report.getWindspeedMax(),
                report.getUvIndexMax(),
                uvLevel.name(),
                report.getSunrise(),
                report.getSunset()
        );
    }

    private List<ExportData.Pollen> toPollenList(DailyReport report) {
        List<ExportData.Pollen> pollen = new ArrayList<>();
        pollen.add(toPollen("Erle", report.getAlderPollen()));
        pollen.add(toPollen("Birke", report.getBirchPollen()));
        pollen.add(toPollen("Gras", report.getGrassPollen()));
        pollen.add(toPollen("Beifuß", report.getMugwortPollen()));
        pollen.add(toPollen("Olive", report.getOlivePollen()));
        pollen.add(toPollen("Ambrosia", report.getRagweedPollen()));
        return pollen;
    }

    private ExportData.Pollen toPollen(String name, Double value) {
        PollenLevel level = PollenLevel.fromValue(value);
        return new ExportData.Pollen(name, value, level.name(), level.getLabel());
    }

    private List<ExportData.Entry> toEntries(DailyReport report) {
        return allergyEntryService.findByDailyReportId(report.getId()).stream()
                .map(this::toEntry)
                .toList();
    }

    private ExportData.Entry toEntry(AllergyEntry entry) {
        Person person = personLoader.findByName(entry.getPersonName()).orElse(null);
        String personType = person != null ? person.type().name() : null;
        return new ExportData.Entry(
                entry.getPersonName(),
                personType,
                toSymptoms(entry.getSymptoms()),
                entry.getUpdatedAt()
        );
    }

    private List<ExportData.Symptom> toSymptoms(String symptomsStr) {
        if (symptomsStr == null || symptomsStr.isBlank()) return List.of();
        List<ExportData.Symptom> symptoms = new ArrayList<>();
        for (String code : symptomsStr.split(",")) {
            symptomLoader.findByCode(code.trim())
                    .ifPresent(s -> symptoms.add(new ExportData.Symptom(s.code(), s.label(), s.icon())));
        }
        return symptoms;
    }

    private WeatherCondition resolveCondition(DailyReport report) {
        return report.getWeatherCode() != null
                ? WeatherCondition.fromCode(report.getWeatherCode())
                : WeatherCondition.UNKNOWN;
    }
}
