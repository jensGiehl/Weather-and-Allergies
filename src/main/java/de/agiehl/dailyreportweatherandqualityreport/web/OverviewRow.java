package de.agiehl.dailyreportweatherandqualityreport.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record OverviewRow(
        LocalDate reportDate,
        String dateFormatted,
        String weatherEmoji,
        String weatherLabel,
        String tempMax,
        String tempMin,
        boolean hasAnyEntry,
        Map<String, List<SymptomDisplay>> symptomsByPerson,
        List<PollenDisplay> pollenEntries
) {
    public record SymptomDisplay(String icon, String label) {}
    public record PollenDisplay(String name, String emoji, String badgeClass) {}
}
