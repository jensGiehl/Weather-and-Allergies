package de.agiehl.dailyreportweatherandqualityreport.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.agiehl.dailyreportweatherandqualityreport.config.TelegramProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramClient {

    private final TelegramProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public void sendMessage(String text) throws IOException, InterruptedException {
        String url = properties.apiUrl() + "/bot" + properties.botToken() + "/sendMessage";
        String body = buildRequestBody(text);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.debug("Telegram API response status: {}", response.statusCode());

        if (response.statusCode() != 200) {
            log.error("Telegram API error {}: {}", response.statusCode(), response.body());
        }
    }

    private String buildRequestBody(String text) throws JsonProcessingException {
        Map<String, String> payload = Map.of(
                "chat_id", properties.chatId(),
                "text", text
        );
        return objectMapper.writeValueAsString(payload);
    }
}
