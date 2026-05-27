package de.agiehl.dailyreportweatherandqualityreport.report;

import de.agiehl.dailyreportweatherandqualityreport.config.WeatherProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class WeeklySummaryFormatter {

    private static final String DIVIDER = "────────────────────────";
    private static final DateTimeFormatter RANGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.");
    private static final DateTimeFormatter RANGE_END_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final WeatherProperties weatherProperties;

    public String format(WeeklySummary summary, String overviewLink) {
        StringBuilder sb = new StringBuilder();

        sb.append("🌿 Wochenrückblick – ").append(weatherProperties.locationName()).append("\n");
        sb.append("📅 ").append(summary.weekStart().format(RANGE_DATE_FORMATTER))
                .append(" – ").append(summary.weekEnd().format(RANGE_END_FORMATTER)).append("\n");
        sb.append(DIVIDER).append("\n");

        appendWeather(sb, summary);
        sb.append(DIVIDER).append("\n");

        appendAllergies(sb, summary);
        sb.append(DIVIDER).append("\n");

        sb.append("📊 Übersicht: ").append(overviewLink);

        return sb.toString();
    }

    private void appendWeather(StringBuilder sb, WeeklySummary summary) {
        sb.append("🌤️ Wetter\n");
        sb.append("   ").append(String.join(" ", summary.dailyWeatherEmojis())).append("\n");
        sb.append("   ↑ Ø ").append(formatTemp(summary.averageTemperatureMax()));
        sb.append("  |  ↓ Ø ").append(formatTemp(summary.averageTemperatureMin())).append("\n");
    }

    private void appendAllergies(StringBuilder sb, WeeklySummary summary) {
        sb.append("🤒 Allergie-Symptome\n\n");

        if (summary.personSummaries().isEmpty()) {
            sb.append("Diese Woche wurden keine Symptome erfasst. 🎉\n");
            return;
        }

        for (WeeklySummary.PersonAllergySummary person : summary.personSummaries()) {
            appendPerson(sb, person, summary.totalDays());
        }
    }

    private void appendPerson(StringBuilder sb, WeeklySummary.PersonAllergySummary person, int totalDays) {
        sb.append(person.personEmoji()).append(" ").append(person.personName())
                .append(" — ").append(person.dayCount()).append(" von ").append(totalDays).append(" Tagen\n");
        for (WeeklySummary.SymptomCount symptom : person.symptomCounts()) {
            sb.append("   • ").append(symptom.symptomLabel())
                    .append(" (").append(symptom.count()).append("×)\n");
        }
        sb.append("\n");
    }

    private String formatTemp(Double value) {
        return value != null ? String.format(Locale.GERMAN, "%.1f °C", value) : "–";
    }
}
