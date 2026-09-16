package de.agiehl.dailyreportweatherandqualityreport.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class VersionAdvice {

    private final String commitId;

    public VersionAdvice(@Value("${git.commit.id.abbrev:lokal}") String commitId) {
        this.commitId = commitId;
    }

    @ModelAttribute("commitId")
    public String commitId() {
        return commitId;
    }
}
