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
import java.util.Objects;

import static java.util.Locale.GERMAN;

@Component
@RequiredArgsConstructor
public class ReportFormatter {

    private static final String DIVIDER = "────────────────────────";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", GERMAN);

    private final WeatherProperties weatherProperties;
    private final SchoolClothingAdvisor schoolClothingAdvisor;

    public String format(WeatherApiResponse weather, PollenApiResponse pollen, String reportLink, String detailLink) {
        WeatherApiResponse.DailyWeather daily = weather.daily();
        PollenApiResponse.Hourly hourly = pollen.hourly();

        WeatherCondition condition = resolveCondition(daily.weathercode());
        UvIndexLevel uvLevel = UvIndexLevel.fromValue(firstOf(daily.uvIndexMax()));
        Integer aqiValue = maxInt(hourly != null ? hourly.europeanAqi() : null);
        EuropeanAqiLevel aqiLevel = EuropeanAqiLevel.fromValue(aqiValue);

        StringBuilder sb = new StringBuilder();

        sb.append(condition.getEmoji()).append(" Tagesbericht – ").append(weatherProperties.locationName()).append("\n");
        sb.append("📅 ").append(formatDate()).append("\n");
        sb.append(DIVIDER).append("\n");

        sb.append("🌡️ Wetterlage: ").append(condition.getLabel()).append("\n\n");

        sb.append("🌡️ Temperatur\n");
        sb.append("   ↑ ").append(formatTemp(firstOf(daily.temperatureMax())));
        sb.append(maxTemperatureTime(weather));
        sb.append("  |  ↓ ").append(formatTemp(firstOf(daily.temperatureMin()))).append("\n\n");
        sb.append(hourlyTemperatures(weather));

        sb.append("🧥 Kleidung für die Schule (8–14 Uhr)\n");
        sb.append("   ").append(schoolClothingAdvisor.recommend(weather)).append("\n\n");

        sb.append("🌧️ Niederschlag\n");
        sb.append("   Menge: ").append(formatMm(firstOf(daily.precipitationSum())));
        sb.append("  |  Chance: ").append(formatPercent(firstOfInt(daily.precipitationProbability()))).append("\n\n");

        sb.append("💨 Wind\n");
        sb.append("   Max: ").append(formatKmh(firstOf(daily.windspeedMax()))).append("\n\n");

        sb.append("🔆 UV-Index: ").append(formatUv(firstOf(daily.uvIndexMax()), uvLevel)).append("\n\n");

        sb.append("🌅 Aufgang:   ").append(formatTime(firstOfStr(daily.sunrise()))).append(" Uhr\n");
        sb.append("🌇 Untergang: ").append(formatTime(firstOfStr(daily.sunset()))).append(" Uhr\n");

        sb.append(DIVIDER).append("\n");
        sb.append("🌫️ Luftqualität: ").append(formatAqi(aqiValue, aqiLevel)).append("\n");
        sb.append(DIVIDER).append("\n");
        sb.append("🌿 Pollenflug heute\n\n");

        sb.append("🌳 Erle:      ").append(PollenLevel.fromValue(maxOf(hourly != null ? hourly.alderPollen() : null)).formatted()).append("\n");
        sb.append("🌳 Birke:     ").append(PollenLevel.fromValue(maxOf(hourly != null ? hourly.birchPollen() : null)).formatted()).append("\n");
        sb.append("🌾 Gras:      ").append(PollenLevel.fromValue(maxOf(hourly != null ? hourly.grassPollen() : null)).formatted()).append("\n");
        sb.append("🌿 Beifuß:    ").append(PollenLevel.fromValue(maxOf(hourly != null ? hourly.mugwortPollen() : null)).formatted()).append("\n");
        sb.append("🫒 Olive:     ").append(PollenLevel.fromValue(maxOf(hourly != null ? hourly.olivePollen() : null)).formatted()).append("\n");
        sb.append("🌼 Ambrosia:  ").append(PollenLevel.fromValue(maxOf(hourly != null ? hourly.ragweedPollen() : null)).formatted()).append("\n");

        sb.append(DIVIDER).append("\n");
        sb.append("🔍 Details:\n");
        sb.append(detailLink);
        sb.append("\n\n");

        sb.append(DIVIDER).append("\n");
        sb.append("📋 Symptome erfassen:\n");
        sb.append(reportLink);

        return sb.toString();
    }

    private String maxTemperatureTime(WeatherApiResponse weather) {
        var hourly = weather.hourly();
        String date = firstOfStr(weather.daily().time());
        if (hourly == null || hourly.time() == null || date == null) {
            return "";
        }

        Double maximum = null;
        String maximumTime = null;
        for (int index = 0; index < hourly.time().size(); index++) {
            String time = hourly.time().get(index);
            Double temperature = valueAt(hourly.temperature(), index);
            if (time != null && time.startsWith(date + "T") && temperature != null
                    && (maximum == null || temperature > maximum)) {
                maximum = temperature;
                maximumTime = time;
            }
        }
        return maximumTime == null ? "" : " um " + formatTime(maximumTime) + " Uhr";
    }

    private String hourlyTemperatures(WeatherApiResponse weather) {
        var hourly = weather.hourly();
        String date = firstOfStr(weather.daily().time());
        StringBuilder result = new StringBuilder();
        for (int hour : List.of(8, 12, 14)) {
            int index = hourly != null && hourly.time() != null && date != null
                    ? hourly.time().indexOf("%sT%02d:00".formatted(date, hour)) : -1;
            Double temperature = hourly != null ? valueAt(hourly.temperature(), index) : null;
            Integer code = hourly != null ? valueAt(hourly.weatherCode(), index) : null;
            WeatherCondition condition = code != null ? WeatherCondition.fromCode(code) : WeatherCondition.UNKNOWN;
            result.append("   Temperatur um %d Uhr: %s %s\n"
                    .formatted(hour, formatTemp(temperature), condition.getEmoji()));
        }
        return result.append("\n").toString();
    }

    private <T> T valueAt(List<T> values, int index) {
        return values != null && index >= 0 && index < values.size() ? values.get(index) : null;
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

    private String formatAqi(Integer value, EuropeanAqiLevel level) {
        if (value == null) return "–";
        return value + " (" + level.formatted() + ")";
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

    private Double maxOf(List<Double> list) {
        if (list == null) return null;
        return list.stream().filter(Objects::nonNull).max(Double::compareTo).orElse(null);
    }

    private Integer maxInt(List<Integer> list) {
        if (list == null) return null;
        return list.stream().filter(Objects::nonNull).max(Integer::compareTo).orElse(null);
    }
}
