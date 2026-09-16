package de.agiehl.dailyreportweatherandqualityreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
@EnableRetry
@PropertySource(value = "classpath:git.properties", ignoreResourceNotFound = true)
public class DailyReportWeatherAndQualityReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailyReportWeatherAndQualityReportApplication.class, args);
    }
}
