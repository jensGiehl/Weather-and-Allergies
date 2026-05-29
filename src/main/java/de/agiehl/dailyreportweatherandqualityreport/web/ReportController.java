package de.agiehl.dailyreportweatherandqualityreport.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.agiehl.dailyreportweatherandqualityreport.domain.*;
import de.agiehl.dailyreportweatherandqualityreport.report.EuropeanAqiLevel;
import de.agiehl.dailyreportweatherandqualityreport.report.PollenLevel;
import de.agiehl.dailyreportweatherandqualityreport.report.UvIndexLevel;
import de.agiehl.dailyreportweatherandqualityreport.report.WeatherCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", Locale.GERMAN);
    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    private final DailyReportService dailyReportService;
    private final AllergyEntryService allergyEntryService;
    private final PersonLoader personLoader;
    private final SymptomLoader symptomLoader;
    private final ObjectMapper objectMapper;

    @GetMapping("/report/{id}")
    public String showReport(@PathVariable String id, Model model) throws JsonProcessingException {
        DailyReport report = dailyReportService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bericht nicht gefunden"));

        model.addAttribute("reportId", id);
        model.addAttribute("date", formatDate(report.getReportDate()));
        model.addAttribute("weatherCondition", resolveCondition(report));
        model.addAttribute("temperatureMax", formatTemp(report.getTemperatureMax()));
        model.addAttribute("temperatureMin", formatTemp(report.getTemperatureMin()));
        model.addAttribute("precipitationSum", formatMm(report.getPrecipitationSum()));
        model.addAttribute("precipitationProbability", formatPercent(report.getPrecipitationProbability()));
        model.addAttribute("windspeedMax", formatKmh(report.getWindspeedMax()));
        model.addAttribute("uvIndexMax", formatUvValue(report.getUvIndexMax()));
        model.addAttribute("uvLevel", UvIndexLevel.fromValue(report.getUvIndexMax()));
        model.addAttribute("sunrise", nullSafe(report.getSunrise()));
        model.addAttribute("sunset", nullSafe(report.getSunset()));
        model.addAttribute("europeanAqi", formatAqiValue(report.getEuropeanAqi()));
        model.addAttribute("europeanAqiLevel", EuropeanAqiLevel.fromValue(report.getEuropeanAqi()));
        model.addAttribute("pollenEntries", buildPollenEntries(report));
        model.addAttribute("persons", personLoader.getPersons());
        model.addAttribute("symptoms", symptomLoader.getSymptoms());
        model.addAttribute("existingEntriesJson", buildExistingEntriesJson(id));

        return "report";
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    private WeatherCondition resolveCondition(DailyReport report) {
        return report.getWeatherCode() != null
                ? WeatherCondition.fromCode(report.getWeatherCode())
                : WeatherCondition.UNKNOWN;
    }

    private String formatTemp(Double value) {
        return value != null ? String.format(Locale.GERMAN, "%.1f", value) : "–";
    }

    private String formatMm(Double value) {
        return value != null ? String.format(Locale.GERMAN, "%.1f mm", value) : "–";
    }

    private String formatPercent(Integer value) {
        return value != null ? value + " %" : "–";
    }

    private String formatKmh(Double value) {
        return value != null ? String.format(Locale.GERMAN, "%.0f km/h", value) : "–";
    }

    private String formatUvValue(Double value) {
        return value != null ? String.format(Locale.GERMAN, "%.1f", value) : "–";
    }

    private String formatAqiValue(Integer value) {
        return value != null ? value.toString() : "–";
    }

    private String nullSafe(String value) {
        return value != null ? value : "–";
    }

    private List<Map<String, String>> buildPollenEntries(DailyReport report) {
        List<Map<String, String>> entries = new ArrayList<>();
        entries.add(pollenEntry("Erle", report.getAlderPollen()));
        entries.add(pollenEntry("Birke", report.getBirchPollen()));
        entries.add(pollenEntry("Gras", report.getGrassPollen()));
        entries.add(pollenEntry("Beifuß", report.getMugwortPollen()));
        entries.add(pollenEntry("Olive", report.getOlivePollen()));
        entries.add(pollenEntry("Ambrosia", report.getRagweedPollen()));
        return entries;
    }

    private Map<String, String> pollenEntry(String name, Double value) {
        PollenLevel level = PollenLevel.fromValue(value);
        return Map.of(
                "name", name,
                "emoji", level.getEmoji(),
                "label", level.getLabel(),
                "badgeClass", pollenBadgeClass(level)
        );
    }

    private String pollenBadgeClass(PollenLevel level) {
        return switch (level) {
            case NONE     -> "bg-secondary";
            case LOW      -> "bg-success";
            case MEDIUM   -> "bg-warning text-dark";
            case HIGH     -> "bg-danger";
            case VERY_HIGH -> "bg-dark";
        };
    }

    private String buildExistingEntriesJson(String dailyReportId) throws JsonProcessingException {
        Map<String, List<String>> entries = allergyEntryService.findByDailyReportId(dailyReportId).stream()
                .collect(Collectors.toMap(
                        AllergyEntry::getPersonName,
                        entry -> {
                            String symptoms = entry.getSymptoms();
                            if (symptoms == null || symptoms.isBlank()) return List.of();
                            return List.of(symptoms.split(","));
                        }
                ));
        return objectMapper.writeValueAsString(entries);
    }
}
