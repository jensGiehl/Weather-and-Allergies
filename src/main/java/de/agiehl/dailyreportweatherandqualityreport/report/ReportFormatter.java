package de.agiehl.dailyreportweatherandqualityreport.report;

import de.agiehl.dailyreportweatherandqualityreport.config.WeatherProperties;
import de.agiehl.dailyreportweatherandqualityreport.pollen.model.PollenApiResponse;
import de.agiehl.dailyreportweatherandqualityreport.weather.model.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static java.util.Locale.GERMAN;

@Component
@RequiredArgsConstructor
public class ReportFormatter {

    private static final String DIVIDER = "────────────────────────";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", GERMAN);

    private final WeatherProperties weatherProperties;

    public String format(WeatherApiResponse weather, PollenApiResponse pollen, String reportLink) {
        WeatherApiResponse.DailyWeather daily = weather.daily();
        PollenApiResponse.DailyPollen dailyPollen = pollen.daily();

        WeatherCondition condition = resolveCondition(daily.weathercode());
        UvIndexLevel uvLevel = UvIndexLevel.fromValue(firstOf(daily.uvIndexMax()));

        StringBuilder sb = new StringBuilder();

        sb.append(condition.getEmoji()).append(" Tagesbericht – ").append(weatherProperties.locationName()).append("\n");
        sb.append("📅 ").append(formatDate()).append("\n");
        sb.append(DIVIDER).append("\n");

        sb.append("🌡️ Wetterlage: ").append(condition.getLabel()).append("\n\n");

        sb.append("🌡️ Temperatur\n");
        sb.append("   ↑ ").append(formatTemp(firstOf(daily.temperatureMax())));
        sb.append("  |  ↓ ").append(formatTemp(firstOf(daily.temperatureMin()))).append("\n\n");

        sb.append("🌧️ Niederschlag\n");
        sb.append("   Menge: ").append(formatMm(firstOf(daily.precipitationSum())));
        sb.append("  |  Chance: ").append(formatPercent(firstOfInt(daily.precipitationProbability()))).append("\n\n");

        sb.append("💨 Wind\n");
        sb.append("   Max: ").append(formatKmh(firstOf(daily.windspeedMax()))).append("\n\n");

        sb.append("🔆 UV-Index: ").append(formatUv(firstOf(daily.uvIndexMax()), uvLevel)).append("\n\n");

        sb.append("🌅 Aufgang:   ").append(formatTime(firstOfStr(daily.sunrise()))).append(" Uhr\n");
        sb.append("🌇 Untergang: ").append(formatTime(firstOfStr(daily.sunset()))).append(" Uhr\n");

        sb.append(DIVIDER).append("\n");
        sb.append("🌿 Pollenflug heute\n\n");

        sb.append("🌳 Erle:      ").append(PollenLevel.fromValue(firstOf(dailyPollen.alderPollen())).formatted()).append("\n");
        sb.append("🌳 Birke:     ").append(PollenLevel.fromValue(firstOf(dailyPollen.birchPollen())).formatted()).append("\n");
        sb.append("🌾 Gras:      ").append(PollenLevel.fromValue(firstOf(dailyPollen.grassPollen())).formatted()).append("\n");
        sb.append("🌿 Beifuß:    ").append(PollenLevel.fromValue(firstOf(dailyPollen.mugwortPollen())).formatted()).append("\n");
        sb.append("🫒 Olive:     ").append(PollenLevel.fromValue(firstOf(dailyPollen.olivePollen())).formatted()).append("\n");
        sb.append("🌼 Ambrosia:  ").append(PollenLevel.fromValue(firstOf(dailyPollen.ragweedPollen())).formatted()).append("\n");

        sb.append(DIVIDER).append("\n");
        sb.append("📋 Symptome erfassen:\n");
        sb.append(reportLink);

        return sb.toString();
    }

    private String formatDate() {
        return LocalDate.now(ZoneId.of(weatherProperties.timezone())).format(DATE_FORMATTER);
    }

    private WeatherCondition resolveCondition(List<Integer> codes) {
        Integer code = firstOfInt(codes);
        return code != null ? WeatherCondition.fromCode(code) : WeatherCondition.UNKNOWN;
    }

    private String formatTemp(Double value) {
        return value != null ? String.format(GERMAN, "%.1f °C", value) : "–";
    }

    private String formatMm(Double value) {
        return value != null ? String.format(GERMAN, "%.1f mm", value) : "–";
    }

    private String formatPercent(Integer value) {
        return value != null ? value + " %" : "–";
    }

    private String formatKmh(Double value) {
        return value != null ? String.format(GERMAN, "%.1f km/h", value) : "–";
    }

    private String formatUv(Double value, UvIndexLevel level) {
        if (value == null) return "–";
        return String.format(GERMAN, "%.1f (%s)", value, level.formatted());
    }

    private String formatTime(String isoDateTime) {
        if (isoDateTime == null || !isoDateTime.contains("T")) return "–";
        return isoDateTime.substring(isoDateTime.indexOf("T") + 1);
    }

    private Double firstOf(List<Double> list) {
        return (list != null && !list.isEmpty()) ? list.getFirst() : null;
    }

    private Integer firstOfInt(List<Integer> list) {
        return (list != null && !list.isEmpty()) ? list.getFirst() : null;
    }

    private String firstOfStr(List<String> list) {
        return (list != null && !list.isEmpty()) ? list.getFirst() : null;
    }
}
