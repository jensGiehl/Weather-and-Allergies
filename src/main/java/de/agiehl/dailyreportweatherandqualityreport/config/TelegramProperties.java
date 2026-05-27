package de.agiehl.dailyreportweatherandqualityreport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties(
        String botToken,
        String chatId,
        String apiUrl
) {}
