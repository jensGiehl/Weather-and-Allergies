package de.agiehl.dailyreportweatherandqualityreport.report;

import java.time.LocalDate;
import java.util.List;

public record WeeklySummary(
        LocalDate weekStart,
        LocalDate weekEnd,
        int totalDays,
        List<String> dailyWeatherEmojis,
        Double averageTemperatureMax,
        Double averageTemperatureMin,
        List<PersonAllergySummary> personSummaries
) {
    public record PersonAllergySummary(
            String personName,
            String personEmoji,
            int dayCount,
            List<SymptomCount> symptomCounts
    ) {}

    public record SymptomCount(
            String symptomLabel,
            String symptomIcon,
            int count
    ) {}
}
