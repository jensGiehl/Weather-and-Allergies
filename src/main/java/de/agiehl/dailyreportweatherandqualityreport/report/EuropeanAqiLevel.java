package de.agiehl.dailyreportweatherandqualityreport.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EuropeanAqiLevel {
    GOOD("🟢", "Sehr gut"),
    FAIR("🟡", "Gut"),
    MODERATE("🟠", "Mäßig"),
    POOR("🔴", "Schlecht"),
    VERY_POOR("🟣", "Sehr schlecht"),
    EXTREMELY_POOR("🚨", "Extrem schlecht");

    private final String emoji;
    private final String label;

    public static EuropeanAqiLevel fromValue(Integer value) {
        if (value == null || value <= 20) return GOOD;
        if (value <= 40) return FAIR;
        if (value <= 60) return MODERATE;
        if (value <= 80) return POOR;
        if (value <= 100) return VERY_POOR;
        return EXTREMELY_POOR;
    }

    public String formatted() {
        return emoji + " " + label;
    }
}
