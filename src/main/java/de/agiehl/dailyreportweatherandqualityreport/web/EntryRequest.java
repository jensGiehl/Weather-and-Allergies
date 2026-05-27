package de.agiehl.dailyreportweatherandqualityreport.web;

import java.util.List;

public record EntryRequest(String personName, List<String> symptoms) {}
