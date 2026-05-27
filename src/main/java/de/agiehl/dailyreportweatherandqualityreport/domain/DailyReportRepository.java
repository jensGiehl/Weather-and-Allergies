package de.agiehl.dailyreportweatherandqualityreport.domain;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportRepository extends CrudRepository<DailyReport, String> {

    Optional<DailyReport> findByReportDate(LocalDate date);

    List<DailyReport> findAllByOrderByReportDateDesc();

    List<DailyReport> findAllByReportDateBetweenOrderByReportDateAsc(LocalDate start, LocalDate end);
}
