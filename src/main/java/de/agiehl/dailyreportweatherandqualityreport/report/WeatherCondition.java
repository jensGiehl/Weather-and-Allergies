package de.agiehl.dailyreportweatherandqualityreport.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherCondition {
    CLEAR_SKY(0, "☀️", "Klarer Himmel"),
    MAINLY_CLEAR(1, "🌤️", "Überwiegend klar"),
    PARTLY_CLOUDY(2, "⛅", "Teilweise bewölkt"),
    OVERCAST(3, "☁️", "Bedeckt"),
    FOG(45, "🌫️", "Nebel"),
    RIME_FOG(48, "🌫️", "Gefrierender Nebel"),
    LIGHT_DRIZZLE(51, "🌦️", "Leichter Sprühregen"),
    MODERATE_DRIZZLE(53, "🌦️", "Mäßiger Sprühregen"),
    DENSE_DRIZZLE(55, "🌧️", "Dichter Sprühregen"),
    FREEZING_DRIZZLE_LIGHT(56, "🌦️", "Gefrierender Sprühregen"),
    FREEZING_DRIZZLE_HEAVY(57, "🌧️", "Starker gefrierender Sprühregen"),
    LIGHT_RAIN(61, "🌧️", "Leichter Regen"),
    MODERATE_RAIN(63, "🌧️", "Mäßiger Regen"),
    HEAVY_RAIN(65, "🌧️", "Starker Regen"),
    FREEZING_RAIN_LIGHT(66, "🌧️", "Gefrierender Regen"),
    FREEZING_RAIN_HEAVY(67, "🌧️", "Starker gefrierender Regen"),
    LIGHT_SNOW(71, "🌨️", "Leichter Schneefall"),
    MODERATE_SNOW(73, "🌨️", "Mäßiger Schneefall"),
    HEAVY_SNOW(75, "❄️", "Starker Schneefall"),
    SNOW_GRAINS(77, "🌨️", "Schneekörner"),
    LIGHT_SHOWERS(80, "🌦️", "Leichte Regenschauer"),
    MODERATE_SHOWERS(81, "🌧️", "Mäßige Regenschauer"),
    VIOLENT_SHOWERS(82, "⛈️", "Starke Regenschauer"),
    LIGHT_SNOW_SHOWERS(85, "🌨️", "Leichte Schneeschauer"),
    HEAVY_SNOW_SHOWERS(86, "🌨️", "Starke Schneeschauer"),
    THUNDERSTORM(95, "⛈️", "Gewitter"),
    THUNDERSTORM_LIGHT_HAIL(96, "⛈️", "Gewitter mit leichtem Hagel"),
    THUNDERSTORM_HEAVY_HAIL(99, "⛈️", "Gewitter mit starkem Hagel"),
    UNKNOWN(-1, "❓", "Unbekannt");

    private final int code;
    private final String emoji;
    private final String label;

    public static WeatherCondition fromCode(int code) {
        for (WeatherCondition condition : values()) {
            if (condition.code == code) {
                return condition;
            }
        }
        return UNKNOWN;
    }

    public String formatted() {
        return emoji + " " + label;
    }
}
