package de.agiehl.dailyreportweatherandqualityreport.web;

import de.agiehl.dailyreportweatherandqualityreport.domain.AllergyEntryService;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AllergyEntryController {

    private final AllergyEntryService allergyEntryService;
    private final DailyReportService dailyReportService;

    @PostMapping("/report/{id}/entry")
    public ResponseEntity<Map<String, String>> saveEntry(
            @PathVariable String id,
            @RequestBody EntryRequest request) {

        if (dailyReportService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (request.personName() == null || request.personName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "personName required"));
        }
        if (request.symptoms() == null || request.symptoms().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "at least one symptom required"));
        }

        allergyEntryService.save(id, request.personName(), request.symptoms());
        return ResponseEntity.ok(Map.of("status", "saved"));
    }
}
