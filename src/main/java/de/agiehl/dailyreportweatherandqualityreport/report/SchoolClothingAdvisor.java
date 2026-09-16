package de.agiehl.dailyreportweatherandqualityreport.report;

import de.agiehl.dailyreportweatherandqualityreport.weather.model.WeatherApiResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SchoolClothingAdvisor {

    public String recommend(WeatherApiResponse weather) {
        String date = weather.daily().time() == null || weather.daily().time().isEmpty()
                ? null : weather.daily().time().getFirst();
        var hourly = weather.hourly();
        if (date == null || hourly == null || hourly.time() == null) {
            return "Für 8–14 Uhr fehlen Stundenwerte; bitte die aktuelle Vorhersage prüfen.";
        }

        List<Double> temperatures = new ArrayList<>();
        boolean rain = false;
        boolean snow = false;
        for (int hour = 8; hour <= 14; hour++) {
            int index = hourly.time().indexOf("%sT%02d:00".formatted(date, hour));
            if (index < 0) {
                continue;
            }
            Double temperature = valueAt(hourly.temperature(), index);
            if (temperature != null) {
                temperatures.add(temperature);
            }
            Integer code = valueAt(hourly.weatherCode(), index);
            if (code != null) {
                rain |= (code >= 51 && code <= 67) || (code >= 80 && code <= 82) || code >= 95 && code <= 99;
                snow |= (code >= 71 && code <= 77) || (code >= 85 && code <= 86);
            }
        }

        if (temperatures.isEmpty()) {
            return "Für 8–14 Uhr fehlen Temperaturwerte; bitte die aktuelle Vorhersage prüfen.";
        }

        double minimum = temperatures.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        double maximum = temperatures.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
        String clothing;
        if (minimum < 5) {
            clothing = "Winterjacke, Pullover, lange Hose und warme Schuhe";
        } else if (minimum < 12) {
            clothing = "Warme Jacke, Pullover, lange Hose und feste Schuhe";
        } else if (minimum < 18) {
            clothing = "Leichte Jacke oder Hoodie, lange Hose und geschlossene Schuhe";
        } else if (minimum < 24) {
            clothing = "T-Shirt, leichte Hose und eine dünne Jacke für den Morgen";
        } else {
            clothing = "Leichte Kleidung und luftige Schuhe";
        }

        StringBuilder recommendation = new StringBuilder(clothing);
        if (maximum - minimum >= 8) {
            recommendation.append("; am besten in Schichten zum Ausziehen");
        }
        if (rain) {
            recommendation.append("; Regenjacke und Schirm mitnehmen");
        }
        if (snow) {
            recommendation.append("; wasserdichte Schuhe anziehen");
        }
        return recommendation.append('.').toString();
    }

    private <T> T valueAt(List<T> values, int index) {
        return values != null && index < values.size() ? values.get(index) : null;
    }
}
