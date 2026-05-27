package de.agiehl.dailyreportweatherandqualityreport.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("allergy_entry")
public class AllergyEntry implements Persistable<String> {

    @Id
    private String id;

    @Transient
    private boolean isNew;
    private String dailyReportId;
    private String personName;
    private String symptoms;
    private LocalDateTime updatedAt;
}
