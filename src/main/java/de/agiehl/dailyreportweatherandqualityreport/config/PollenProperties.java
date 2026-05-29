package de.agiehl.dailyreportweatherandqualityreport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pollen")
public record PollenProperties(
        String apiUrl,
        String hourlyVariables
) {}
