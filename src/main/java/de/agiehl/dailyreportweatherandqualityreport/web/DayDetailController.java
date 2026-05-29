package de.agiehl.dailyreportweatherandqualityreport.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReport;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReportService;
import de.agiehl.dailyreportweatherandqualityreport.domain.HourlyReading;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@Controller
@RequiredArgsConstructor
public class DayDetailController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", Locale.GERMAN);

    private final DailyReportService dailyReportService;
    private final ObjectMapper objectMapper;

    @GetMapping("/day/{id}")
    public String showDay(@PathVariable String id, Model model) throws JsonProcessingException {
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
        model.addAttribute("temperatureSeriesJson", buildTemperatureSeriesJson(id));
        model.addAttribute("pollenChartsJson", buildPollenChartsJson(id));
        model.addAttribute("pollenEntries", buildPollenEntries(report));

        return "day";
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
            case NONE      -> "bg-secondary";
            case LOW       -> "bg-success";
            case MEDIUM    -> "bg-warning text-dark";
            case HIGH      -> "bg-danger";
            case VERY_HIGH -> "bg-dark";
        };
    }

    private String buildTemperatureSeriesJson(String dailyReportId) throws JsonProcessingException {
        List<Map<String, Object>> points = dailyReportService.findHourlyReadings(dailyReportId).stream()
                .filter(reading -> reading.getTemperature() != null)
                .map(reading -> Map.<String, Object>of(
                        "hour", reading.getHourOfDay(),
                        "temperature", reading.getTemperature()
                ))
                .toList();
        return objectMapper.writeValueAsString(points);
    }

    private String buildPollenChartsJson(String dailyReportId) throws JsonProcessingException {
        List<HourlyReading> readings = dailyReportService.findHourlyReadings(dailyReportId);
        List<Map<String, Object>> charts = new ArrayList<>();
        addPollenChartIfActive(charts, readings, "Erle",     "🌳", "#8d6e63", HourlyReading::getAlderPollen);
        addPollenChartIfActive(charts, readings, "Birke",    "🌳", "#81c784", HourlyReading::getBirchPollen);
        addPollenChartIfActive(charts, readings, "Gras",     "🌾", "#c0ca33", HourlyReading::getGrassPollen);
        addPollenChartIfActive(charts, readings, "Beifuß",   "🌿", "#9575cd", HourlyReading::getMugwortPollen);
        addPollenChartIfActive(charts, readings, "Olive",    "🫒", "#689f38", HourlyReading::getOlivePollen);
        addPollenChartIfActive(charts, readings, "Ambrosia", "🌼", "#ffb300", HourlyReading::getRagweedPollen);
        return objectMapper.writeValueAsString(charts);
    }

    private void addPollenChartIfActive(List<Map<String, Object>> charts,
                                        List<HourlyReading> readings,
                                        String name,
                                        String emoji,
                                        String color,
                                        Function<HourlyReading, Double> extractor) {
        List<Map<String, Object>> points = readings.stream()
                .filter(r -> extractor.apply(r) != null)
                .map(r -> Map.<String, Object>of(
                        "hour", r.getHourOfDay(),
                        "value", extractor.apply(r)
                ))
                .toList();

        boolean hasNonZero = points.stream()
                .anyMatch(p -> ((Number) p.get("value")).doubleValue() > 0.0);
        if (!hasNonZero) return;

        charts.add(Map.of(
                "name", name,
                "emoji", emoji,
                "color", color,
                "points", points
        ));
    }
}
