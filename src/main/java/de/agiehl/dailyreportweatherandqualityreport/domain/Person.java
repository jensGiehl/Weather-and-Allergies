package de.agiehl.dailyreportweatherandqualityreport.domain;

public record Person(String name, PersonType type) {

    public String emoji() {
        return switch (type) {
            case MANN     -> "👨";
            case FRAU     -> "👩";
            case JUNGE    -> "👦";
            case MAEDCHEN -> "👧";
        };
    }

    public String badgeClass() {
        return switch (type) {
            case MANN    -> "bg-primary-subtle text-primary-emphasis border border-primary-subtle";
            case FRAU    -> "bg-danger-subtle text-danger-emphasis border border-danger-subtle";
            case JUNGE   -> "bg-success-subtle text-success-emphasis border border-success-subtle";
            case MAEDCHEN -> "bg-info-subtle text-info-emphasis border border-info-subtle";
        };
    }
}