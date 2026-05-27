package de.agiehl.dailyreportweatherandqualityreport.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherApiResponse(
        @JsonProperty("daily") DailyWeather daily
) {
    public record DailyWeather(
            @JsonProperty("time") List<String> time,
            @JsonProperty("temperature_2m_max") List<Double> temperatureMax,
            @JsonProperty("temperature_2m_min") List<Double> temperatureMin,
            @JsonProperty("precipitation_sum") List<Double> precipitationSum,
            @JsonProperty("precipitation_probability_max") List<Integer> precipitationProbability,
            @JsonProperty("windspeed_10m_max") List<Double> windspeedMax,
            @JsonProperty("weathercode") List<Integer> weathercode,
            @JsonProperty("uv_index_max") List<Double> uvIndexMax,
            @JsonProperty("sunrise") List<String> sunrise,
            @JsonProperty("sunset") List<String> sunset
    ) {}
}
