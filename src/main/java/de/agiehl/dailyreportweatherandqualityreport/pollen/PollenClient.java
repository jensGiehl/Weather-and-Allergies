package de.agiehl.dailyreportweatherandqualityreport.pollen;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.agiehl.dailyreportweatherandqualityreport.config.PollenProperties;
import de.agiehl.dailyreportweatherandqualityreport.config.WeatherProperties;
import de.agiehl.dailyreportweatherandqualityreport.pollen.model.PollenApiResponse;
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
public class PollenClient {

    private final PollenProperties pollenProperties;
    private final WeatherProperties weatherProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PollenApiResponse fetchTodayPollen() throws IOException, InterruptedException {
        URI uri = buildUri();
        log.info("Fetching air quality from: {}", uri);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Air quality API response status: {}", response.statusCode());

        try {
            return objectMapper.readValue(response.body(), PollenApiResponse.class);
        } catch (Exception ex) {
            log.error("Failed to parse air quality API response: {}", response.body(), ex);
            throw ex;
        }
    }

    private URI buildUri() {
        String url = pollenProperties.apiUrl()
                + "?latitude=" + weatherProperties.latitude()
                + "&longitude=" + weatherProperties.longitude()
                + "&hourly=" + pollenProperties.hourlyVariables()
                + "&timezone=" + weatherProperties.timezone()
                + "&forecast_days=1";
        return URI.create(url);
    }
}