package de.agiehl.dailyreportweatherandqualityreport.report;

import de.agiehl.dailyreportweatherandqualityreport.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklySummaryService {

    private final DailyReportRepository dailyReportRepository;
    private final AllergyEntryRepository allergyEntryRepository;
    private final PersonLoader personLoader;
    private final SymptomLoader symptomLoader;

    public WeeklySummary buildForWeekOf(LocalDate referenceDate) {
        LocalDate weekStart = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        List<DailyReport> reports = dailyReportRepository
                .findAllByReportDateBetweenOrderByReportDateAsc(weekStart, weekEnd);

        return new WeeklySummary(
                weekStart,
                weekEnd,
                7,
                buildDailyWeatherEmojis(reports, weekStart),
                averageOf(reports, DailyReport::getTemperatureMax),
                averageOf(reports, DailyReport::getTemperatureMin),
                buildPersonSummaries(reports)
        );
    }

    private List<String> buildDailyWeatherEmojis(List<DailyReport> reports, LocalDate weekStart) {
        Map<LocalDate, DailyReport> byDate = reports.stream()
                .collect(Collectors.toMap(DailyReport::getReportDate, r -> r));

        List<String> emojis = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            DailyReport report = byDate.get(weekStart.plusDays(i));
            emojis.add(weatherEmoji(report));
        }
        return emojis;
    }

    private String weatherEmoji(DailyReport report) {
        if (report == null || report.getWeatherCode() == null) return "▫️";
        return WeatherCondition.fromCode(report.getWeatherCode()).getEmoji();
    }

    private Double averageOf(List<DailyReport> reports, java.util.function.Function<DailyReport, Double> getter) {
        OptionalDouble average = reports.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();
        return average.isPresent() ? average.getAsDouble() : null;
    }

    private List<WeeklySummary.PersonAllergySummary> buildPersonSummaries(List<DailyReport> reports) {
        Map<String, List<AllergyEntry>> entriesByPerson = collectEntriesByPerson(reports);

        return personLoader.getPersons().stream()
                .map(person -> buildPersonSummary(person, entriesByPerson.getOrDefault(person.name(), List.of())))
                .filter(summary -> summary.dayCount() > 0)
                .toList();
    }

    private Map<String, List<AllergyEntry>> collectEntriesByPerson(List<DailyReport> reports) {
        Map<String, List<AllergyEntry>> byPerson = new HashMap<>();
        for (DailyReport report : reports) {
            for (AllergyEntry entry : allergyEntryRepository.findByDailyReportId(report.getId())) {
                if (hasSymptoms(entry)) {
                    byPerson.computeIfAbsent(entry.getPersonName(), k -> new ArrayList<>()).add(entry);
                }
            }
        }
        return byPerson;
    }

    private boolean hasSymptoms(AllergyEntry entry) {
        return entry.getSymptoms() != null && !entry.getSymptoms().isBlank();
    }

    private WeeklySummary.PersonAllergySummary buildPersonSummary(Person person, List<AllergyEntry> entries) {
        Map<String, Integer> symptomCounts = new LinkedHashMap<>();
        for (AllergyEntry entry : entries) {
            for (String code : entry.getSymptoms().split(",")) {
                String trimmed = code.trim();
                if (!trimmed.isEmpty()) {
                    symptomCounts.merge(trimmed, 1, Integer::sum);
                }
            }
        }

        List<WeeklySummary.SymptomCount> sortedSymptoms = symptomCounts.entrySet().stream()
                .map(this::toSymptomCount)
                .sorted(Comparator.comparingInt(WeeklySummary.SymptomCount::count).reversed())
                .toList();

        return new WeeklySummary.PersonAllergySummary(
                person.name(),
                person.emoji(),
                entries.size(),
                sortedSymptoms
        );
    }

    private WeeklySummary.SymptomCount toSymptomCount(Map.Entry<String, Integer> entry) {
        Optional<Symptom> symptom = symptomLoader.findByCode(entry.getKey());
        String label = symptom.map(Symptom::label).orElse(entry.getKey());
        String icon = symptom.map(Symptom::icon).orElse("");
        return new WeeklySummary.SymptomCount(label, icon, entry.getValue());
    }
}
