package de.agiehl.dailyreportweatherandqualityreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
@EnableRetry
public class DailyReportWeatherAndQualityReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailyReportWeatherAndQualityReportApplication.class, args);
    }
}
