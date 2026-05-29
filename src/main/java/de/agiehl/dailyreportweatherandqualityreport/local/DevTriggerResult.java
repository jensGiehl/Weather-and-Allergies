package de.agiehl.dailyreportweatherandqualityreport.local;

import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReport;

record DevTriggerResult(
        String id,
        String reportDate,
        Double temperatureMax,
        Double temperatureMin,
        Double precipitationSum,
        Integer precipitationProbability,
        Double windspeedMax,
        Double uvIndexMax,
        String sunrise,
        String sunset,
        Integer weatherCode,
        Double alderPollen,
        Double birchPollen,
        Double grassPollen,
        Double mugwortPollen,
        Double olivePollen,
        Double ragweedPollen,
        Integer europeanAqi
) {
    static DevTriggerResult from(DailyReport report) {
        return new DevTriggerResult(
                report.getId(),
                report.getReportDate() != null ? report.getReportDate().toString() : null,
                report.getTemperatureMax(),
                report.getTemperatureMin(),
                report.getPrecipitationSum(),
                report.getPrecipitationProbability(),
                report.getWindspeedMax(),
                report.getUvIndexMax(),
                report.getSunrise(),
                report.getSunset(),
                report.getWeatherCode(),
                report.getAlderPollen(),
                report.getBirchPollen(),
                report.getGrassPollen(),
                report.getMugwortPollen(),
                report.getOlivePollen(),
                report.getRagweedPollen(),
                report.getEuropeanAqi()
        );
    }
}
