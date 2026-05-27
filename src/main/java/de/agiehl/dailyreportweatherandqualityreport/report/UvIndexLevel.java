package de.agiehl.dailyreportweatherandqualityreport.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UvIndexLevel {
    LOW("🟢", "Niedrig"),
    MODERATE("🟡", "Mäßig"),
    HIGH("🟠", "Hoch"),
    VERY_HIGH("🔴", "Sehr hoch"),
    EXTREME("🚨", "Extrem");

    private final String emoji;
    private final String label;

    public static UvIndexLevel fromValue(Double value) {
        if (value == null || value <= 2.0) return LOW;
        if (value <= 5.0) return MODERATE;
        if (value <= 7.0) return HIGH;
        if (value <= 10.0) return VERY_HIGH;
        return EXTREME;
    }

    public String formatted() {
        return emoji + " " + label;
    }
}
