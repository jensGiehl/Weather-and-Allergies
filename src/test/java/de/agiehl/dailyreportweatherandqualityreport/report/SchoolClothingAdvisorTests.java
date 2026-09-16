package de.agiehl.dailyreportweatherandqualityreport.report;

import de.agiehl.dailyreportweatherandqualityreport.weather.model.WeatherApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SchoolClothingAdvisorTests {

    private final SchoolClothingAdvisor advisor = new SchoolClothingAdvisor();

    @Test
    void considersEverySchoolHourAndIgnoresWeatherOutsideTheSchoolWindow() {
        var hourly = new WeatherApiResponse.HourlyWeather(
                List.of("2026-09-08T07:00", "2026-09-08T08:00", "2026-09-08T10:00",
                        "2026-09-08T14:00", "2026-09-08T15:00", "2026-09-09T10:00"),
                List.of(2.0, 10.0, 13.0, 20.0, 28.0, 1.0),
                List.of(71, 0, 61, 0, 71, 71));

        assertThat(advisor.recommend(weather(hourly)))
                .contains("Warme Jacke", "Schichten zum Ausziehen", "Regenjacke und Schirm")
                .doesNotContain("wasserdichte Schuhe", "Winterjacke");
    }

    @Test
    void recommendsWinterClothesAndWaterproofShoesWhenItSnowsAtSchool() {
        var hourly = new WeatherApiResponse.HourlyWeather(
                List.of("2026-09-08T08:00", "2026-09-08T14:00"),
                List.of(-2.0, 1.0), List.of(71, 3));

        assertThat(advisor.recommend(weather(hourly)))
                .contains("Winterjacke", "wasserdichte Schuhe")
                .doesNotContain("Regenjacke und Schirm");
    }

    @Test
    void reportsWhenSchoolTemperaturesAreMissing() {
        var hourly = new WeatherApiResponse.HourlyWeather(
                List.of("2026-09-08T07:00", "2026-09-08T15:00"),
                List.of(10.0, 25.0), List.of(0, 0));

        assertThat(advisor.recommend(weather(hourly)))
                .contains("fehlen Temperaturwerte")
                .doesNotContain("Jacke");
    }

    private WeatherApiResponse weather(WeatherApiResponse.HourlyWeather hourly) {
        var daily = new WeatherApiResponse.DailyWeather(List.of("2026-09-08"),
                null, null, null, null, null, null, null, null, null);
        return new WeatherApiResponse(daily, hourly);
    }
}
