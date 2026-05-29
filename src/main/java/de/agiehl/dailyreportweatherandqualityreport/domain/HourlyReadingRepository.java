package de.agiehl.dailyreportweatherandqualityreport.domain;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HourlyReadingRepository extends CrudRepository<HourlyReading, String> {

    List<HourlyReading> findByDailyReportIdOrderByHourOfDayAsc(String dailyReportId);

    @Modifying
    @Query("DELETE FROM hourly_reading WHERE daily_report_id = :dailyReportId")
    void deleteByDailyReportId(String dailyReportId);
}
