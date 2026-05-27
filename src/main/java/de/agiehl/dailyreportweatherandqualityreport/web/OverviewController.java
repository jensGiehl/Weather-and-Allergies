package de.agiehl.dailyreportweatherandqualityreport.web;

import de.agiehl.dailyreportweatherandqualityreport.domain.*;
import de.agiehl.dailyreportweatherandqualityreport.report.PollenLevel;
import de.agiehl.dailyreportweatherandqualityreport.report.WeatherCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class OverviewController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EE, dd.MM.yyyy", Locale.GERMAN);

    private final DailyReportService dailyReportService;
    private final AllergyEntryService allergyEntryService;
    private final PersonLoader personLoader;
    private final SymptomLoader symptomLoader;

    @GetMapping("/")
    public String overview(Model model) {
        List<DailyReport> reports = dailyReportService.findAllSortedByDateDesc();
        List<OverviewRow> rows = buildRows(reports);

        long entryCount = rows.stream().filter(OverviewRow::hasEntry).count();

        model.addAttribute("rows", rows);
        model.addAttribute("reportCount", reports.size());
        model.addAttribute("entryCount", entryCount);

        return "overview";
    }

    private List<OverviewRow> buildRows(List<DailyReport> reports) {
        List<OverviewRow> rows = new ArrayList<>();
        for (DailyReport report : reports) {
            List<AllergyEntry> entries = allergyEntryService.findByDailyReportId(report.getId());
            if (entries.isEmpty()) {
                rows.add(emptyRow(report));
            } else {
                entries.stream().map(e -> entryRow(report, e)).forEach(rows::add);
            }
        }
        return rows;
    }

    private OverviewRow emptyRow(DailyReport report) {
        WeatherCondition condition = resolveCondition(report);
        return new OverviewRow(
                report.getId(),
                report.getReportDate(),
                formatDate(report),
                condition.getEmoji(),
                condition.getLabel(),
                formatTemp(report.getTemperatureMax()),
                formatTemp(report.getTemperatureMin()),
                false, "", "", "",
                List.of(), buildPollenEntries(report)
        );
    }

    private OverviewRow entryRow(DailyReport report, AllergyEntry entry) {
        WeatherCondition condition = resolveCondition(report);
        Person person = personLoader.findByName(entry.getPersonName()).orElse(null);

        String emoji      = person != null ? person.emoji()      : "👤";
        String badgeClass = person != null ? person.badgeClass() : "bg-secondary-subtle text-secondary-emphasis";

        return new OverviewRow(
                report.getId(),
                report.getReportDate(),
                formatDate(report),
                condition.getEmoji(),
                condition.getLabel(),
                formatTemp(report.getTemperatureMax()),
                formatTemp(report.getTemperatureMin()),
                true,
                entry.getPersonName(),
                emoji,
                badgeClass,
                parseSymptoms(entry.getSymptoms()),
                buildPollenEntries(report)
        );
    }

    private List<OverviewRow.PollenDisplay> buildPollenEntries(DailyReport report) {
        List<OverviewRow.PollenDisplay> entries = new ArrayList<>();
        addIfFlying(entries, "Erle",     report.getAlderPollen());
        addIfFlying(entries, "Birke",    report.getBirchPollen());
        addIfFlying(entries, "Gras",     report.getGrassPollen());
        addIfFlying(entries, "Beifuß",   report.getMugwortPollen());
        addIfFlying(entries, "Olive",    report.getOlivePollen());
        addIfFlying(entries, "Ambrosia", report.getRagweedPollen());
        return entries;
    }

    private void addIfFlying(List<OverviewRow.PollenDisplay> entries, String name, Double value) {
        PollenLevel level = PollenLevel.fromValue(value);
        if (level != PollenLevel.NONE) {
            entries.add(new OverviewRow.PollenDisplay(name, level.getEmoji(), pollenBadgeClass(level)));
        }
    }

    private String pollenBadgeClass(PollenLevel level) {
        return switch (level) {
            case NONE      -> "bg-secondary";
            case LOW       -> "bg-success";
            case MEDIUM    -> "bg-warning text-dark";
            case HIGH      -> "bg-danger";
            case VERY_HIGH -> "bg-dark";
        };
    }

    private List<OverviewRow.SymptomDisplay> parseSymptoms(String symptomsStr) {
        if (symptomsStr == null || symptomsStr.isBlank()) return List.of();
        List<OverviewRow.SymptomDisplay> result = new ArrayList<>();
        for (String code : symptomsStr.split(",")) {
            symptomLoader.findByCode(code.trim())
                    .ifPresent(s -> result.add(new OverviewRow.SymptomDisplay(s.icon(), s.label())));
        }
        return result;
    }

    private WeatherCondition resolveCondition(DailyReport report) {
        return report.getWeatherCode() != null
                ? WeatherCondition.fromCode(report.getWeatherCode())
                : WeatherCondition.UNKNOWN;
    }

    private String formatDate(DailyReport report) {
        return report.getReportDate() != null ? report.getReportDate().format(DATE_FORMATTER) : "–";
    }

    private String formatTemp(Double value) {
        return value != null ? String.format(Locale.GERMAN, "%.1f", value) : "–";
    }
}
