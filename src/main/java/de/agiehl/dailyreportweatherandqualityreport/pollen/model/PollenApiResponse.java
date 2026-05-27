package de.agiehl.dailyreportweatherandqualityreport.pollen.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PollenApiResponse(
        @JsonProperty("daily") DailyPollen daily
) {
    public record DailyPollen(
            @JsonProperty("time") List<String> time,
            @JsonProperty("alder_pollen") List<Double> alderPollen,
            @JsonProperty("birch_pollen") List<Double> birchPollen,
            @JsonProperty("grass_pollen") List<Double> grassPollen,
            @JsonProperty("mugwort_pollen") List<Double> mugwortPollen,
            @JsonProperty("olive_pollen") List<Double> olivePollen,
            @JsonProperty("ragweed_pollen") List<Double> ragweedPollen
    ) {}
}
