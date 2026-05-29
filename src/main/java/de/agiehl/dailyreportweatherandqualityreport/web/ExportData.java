package de.agiehl.dailyreportweatherandqualityreport.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ExportData(
        LocalDate date,
        Weather weather,
        AirQuality airQuality,
        List<Pollen> pollen,
        List<Entry> entries
) {
    public record AirQuality(
            Integer europeanAqi,
            String level,
            String label
    ) {}

    public record Weather(
            Integer code,
            String label,
            Double temperatureMax,
            Double temperatureMin,
            Double precipitationSum,
            Integer precipitationProbability,
            Double windspeedMax,
            Double uvIndexMax,
            String uvLevel,
            String sunrise,
            String sunset
    ) {}

    public record Pollen(
            String name,
            Double value,
            String level,
            String label
    ) {}

    public record Entry(
            String personName,
            String personType,
            List<Symptom> symptoms,
            LocalDateTime updatedAt
    ) {}

    public record Symptom(
            String code,
            String label,
            String icon
    ) {}
}
