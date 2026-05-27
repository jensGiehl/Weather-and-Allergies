package de.agiehl.dailyreportweatherandqualityreport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather")
public record WeatherProperties(
        double latitude,
        double longitude,
        String locationName,
        String timezone,
        String apiUrl,
        String dailyVariables
) {}
