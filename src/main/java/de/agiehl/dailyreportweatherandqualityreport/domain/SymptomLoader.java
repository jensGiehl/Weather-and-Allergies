package de.agiehl.dailyreportweatherandqualityreport.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class SymptomLoader {

    private final List<Symptom> symptoms;

    public SymptomLoader(ObjectMapper objectMapper) throws IOException {
        ClassPathResource resource = new ClassPathResource("symptoms.json");
        List<Symptom> loaded = objectMapper.readValue(
                resource.getInputStream(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Symptom.class)
        );
        this.symptoms = Collections.unmodifiableList(loaded);
    }

    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    public Optional<Symptom> findByCode(String code) {
        return symptoms.stream().filter(s -> s.code().equals(code)).findFirst();
    }
}
