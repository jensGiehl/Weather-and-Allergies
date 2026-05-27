package de.agiehl.dailyreportweatherandqualityreport.web;

import java.time.LocalDate;
import java.util.List;

public record OverviewRow(
        String reportId,
        LocalDate reportDate,
        String dateFormatted,
        String weatherEmoji,
        String weatherLabel,
        String tempMax,
        String tempMin,
        boolean hasEntry,
        String personName,
        String personEmoji,
        String personBadgeClass,
        List<SymptomDisplay> symptoms,
        List<PollenDisplay> pollenEntries
) {
    public record SymptomDisplay(String icon, String label) {}
    public record PollenDisplay(String name, String emoji, String badgeClass) {}
}
