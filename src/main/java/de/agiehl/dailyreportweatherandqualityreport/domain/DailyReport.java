package de.agiehl.dailyreportweatherandqualityreport.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("daily_report")
public class DailyReport implements Persistable<String> {

    @Id
    private String id;

    @Transient
    private boolean isNew;
    private LocalDate reportDate;

    private Integer weatherCode;
    private Double temperatureMax;
    private Double temperatureMin;
    private Double precipitationSum;
    private Integer precipitationProbability;
    private Double windspeedMax;
    private Double uvIndexMax;
    private String sunrise;
    private String sunset;

    private Double alderPollen;
    private Double birchPollen;
    private Double grassPollen;
    private Double mugwortPollen;
    private Double olivePollen;
    private Double ragweedPollen;

    private Integer europeanAqi;

    private LocalDateTime createdAt;
}
