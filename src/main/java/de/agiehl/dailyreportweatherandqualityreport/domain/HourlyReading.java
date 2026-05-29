package de.agiehl.dailyreportweatherandqualityreport.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("hourly_reading")
public class HourlyReading implements Persistable<String> {

    @Id
    private String id;

    @Transient
    private boolean isNew;

    private String dailyReportId;
    private Integer hourOfDay;

    private Double temperature;
    private Double alderPollen;
    private Double birchPollen;
    private Double grassPollen;
    private Double mugwortPollen;
    private Double olivePollen;
    private Double ragweedPollen;
}
