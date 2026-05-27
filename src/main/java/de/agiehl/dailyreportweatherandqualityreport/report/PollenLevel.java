package de.agiehl.dailyreportweatherandqualityreport.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PollenLevel {
    NONE("✅", "keine"),
    LOW("🟢", "gering"),
    MEDIUM("🟡", "mäßig"),
    HIGH("🔴", "hoch"),
    VERY_HIGH("🚨", "sehr hoch");

    private final String emoji;
    private final String label;

    public static PollenLevel fromValue(Double value) {
        if (value == null || value == 0.0) return NONE;
        if (value <= 10.0) return LOW;
        if (value <= 30.0) return MEDIUM;
        if (value <= 100.0) return HIGH;
        return VERY_HIGH;
    }

    public String formatted() {
        return emoji + " " + label;
    }
}
