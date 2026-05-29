package de.agiehl.dailyreportweatherandqualityreport.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.agiehl.dailyreportweatherandqualityreport.domain.AllergyEntry;
import de.agiehl.dailyreportweatherandqualityreport.domain.AllergyEntryService;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReport;
import de.agiehl.dailyreportweatherandqualityreport.domain.DailyReportService;
import de.agiehl.dailyreportweatherandqualityreport.domain.PersonLoader;
import de.agiehl.dailyreportweatherandqualityreport.domain.SymptomLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", Locale.GERMAN);

    private final DailyReportService dailyReportService;
    private final AllergyEntryService allergyEntryService;
    private final PersonLoader personLoader;
    private final SymptomLoader symptomLoader;
    private final ObjectMapper objectMapper;

    @GetMapping("/report/{id}")
    public String showReport(@PathVariable String id, Model model) throws JsonProcessingException {
        DailyReport report = dailyReportService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bericht nicht gefunden"));

        model.addAttribute("reportId", id);
        model.addAttribute("date", formatDate(report.getReportDate()));
        model.addAttribute("reportDateIso", report.getReportDate() != null ? report.getReportDate().toString() : "");
        model.addAttribute("persons", personLoader.getPersons());
        model.addAttribute("symptoms", symptomLoader.getSymptoms());
        model.addAttribute("existingEntriesJson", buildExistingEntriesJson(id));

        return "report";
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    private String buildExistingEntriesJson(String dailyReportId) throws JsonProcessingException {
        Map<String, List<String>> entries = allergyEntryService.findByDailyReportId(dailyReportId).stream()
                .collect(Collectors.toMap(
                        AllergyEntry::getPersonName,
                        entry -> {
                            String symptoms = entry.getSymptoms();
                            if (symptoms == null || symptoms.isBlank()) return List.of();
                            return List.of(symptoms.split(","));
                        }
                ));
        return objectMapper.writeValueAsString(entries);
    }
}
