package de.agiehl.dailyreportweatherandqualityreport.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.agiehl.dailyreportweatherandqualityreport.config.WeatherProperties;
import de.agiehl.dailyreportweatherandqualityreport.weather.model.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherClient {

    private final WeatherProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WeatherApiResponse fetchTodayWeather() throws IOException, InterruptedException {
        URI uri = buildUri();
        log.info("Fetching weather from: {}", uri);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.debug("Weather API response status: {}", response.statusCode());

        return objectMapper.readValue(response.body(), WeatherApiResponse.class);
    }

    private URI buildUri() {
        String url = properties.apiUrl()
                + "?latitude=" + properties.latitude()
                + "&longitude=" + properties.longitude()
                + "&daily=" + properties.dailyVariables()
                + "&hourly=" + properties.hourlyVariables()
                + "&timezone=" + properties.timezone()
                + "&forecast_days=1";
        return URI.create(url);
    }
}