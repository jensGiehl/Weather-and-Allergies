package de.agiehl.dailyreportweatherandqualityreport.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AllergyEntryService {

    private final AllergyEntryRepository repository;

    @Transactional
    public AllergyEntry save(String dailyReportId, String personName, List<String> symptoms) {
        String symptomsStr = String.join(",", symptoms);
        return repository.findByDailyReportIdAndPersonName(dailyReportId, personName)
                .map(existing -> updateEntry(existing, symptomsStr))
                .orElseGet(() -> createEntry(dailyReportId, personName, symptomsStr));
    }

    public List<AllergyEntry> findByDailyReportId(String dailyReportId) {
        return repository.findByDailyReportId(dailyReportId);
    }

    private AllergyEntry updateEntry(AllergyEntry entry, String symptomsStr) {
        entry.setSymptoms(symptomsStr);
        entry.setUpdatedAt(LocalDateTime.now());
        return repository.save(entry);
    }

    private AllergyEntry createEntry(String dailyReportId, String personName, String symptomsStr) {
        AllergyEntry entry = new AllergyEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setNew(true);
        entry.setDailyReportId(dailyReportId);
        entry.setPersonName(personName);
        entry.setSymptoms(symptomsStr);
        entry.setUpdatedAt(LocalDateTime.now());
        return repository.save(entry);
    }
}
