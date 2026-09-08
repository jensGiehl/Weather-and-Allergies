package de.agiehl.dailyreportweatherandqualityreport.report;

import de.agiehl.dailyreportweatherandqualityreport.config.WeatherProperties;
import de.agiehl.dailyreportweatherandqualityreport.pollen.model.PollenApiResponse;
import de.agiehl.dailyreportweatherandqualityreport.weather.model.WeatherApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportFormatterTests {

    private final ReportFormatter formatter = new ReportFormatter(
            new WeatherProperties(52.52, 13.41, "Berlin", "Europe/Berlin", "", "", ""));

    @Test
    void usesHourlyTimestampsAndConditionsAndFirstMaximumOfReportDay() {
        var hourly = new WeatherApiResponse.HourlyWeather(
                List.of("2026-09-08T08:00", "2026-09-08T12:00", "2026-09-08T14:00",
                        "2026-09-08T15:00", "2026-09-09T12:00"),
                List.of(16.0, 23.0, 25.0, 25.0, 30.0),
                List.of(0, 3, 61, 61, 0));

        assertThat(format(hourly)).contains(
                "↑ 25,0 °C um 14:00 Uhr",
                "Temperatur um 8 Uhr: 16,0 °C ☀️",
                "Temperatur um 12 Uhr: 23,0 °C ☁️",
                "Temperatur um 14 Uhr: 25,0 °C 🌧️");
    }

    @Test
    void handlesMissingHourlyData() {
        assertThat(format(null)).contains("↑ 25,0 °C  |", "Temperatur um 8 Uhr: – ❓",
                "Temperatur um 12 Uhr: – ❓", "Temperatur um 14 Uhr: – ❓");
    }

    @Test
    void handlesNullValuesAndShorterLists() {
        var hourly = new WeatherApiResponse.HourlyWeather(
                List.of("2026-09-08T08:00", "2026-09-08T12:00", "2026-09-08T14:00"),
                Arrays.asList(null, 23.0), List.of(999));

        assertThat(format(hourly)).contains("Temperatur um 8 Uhr: – ❓",
                "Temperatur um 12 Uhr: 23,0 °C ❓", "Temperatur um 14 Uhr: – ❓");
    }

    private String format(WeatherApiResponse.HourlyWeather hourly) {
        var daily = new WeatherApiResponse.DailyWeather(List.of("2026-09-08"),
                List.of(25.0), List.of(12.0), null, null, null, List.of(0), null, null, null);
        return formatter.format(new WeatherApiResponse(daily, hourly), new PollenApiResponse(null),
                "https://example.com/report", "https://example.com/details");
    }
}
