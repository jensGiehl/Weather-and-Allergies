package de.agiehl.dailyreportweatherandqualityreport.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AllergyEntryRepository extends CrudRepository<AllergyEntry, String> {

    Optional<AllergyEntry> findByDailyReportIdAndPersonName(String dailyReportId, String personName);

    List<AllergyEntry> findByDailyReportId(String dailyReportId);
}
