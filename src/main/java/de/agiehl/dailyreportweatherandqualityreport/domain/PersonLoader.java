package de.agiehl.dailyreportweatherandqualityreport.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class PersonLoader {

    private final List<Person> persons;

    public PersonLoader(ObjectMapper objectMapper) throws IOException {
        ClassPathResource resource = new ClassPathResource("persons.json");
        List<Person> loaded = objectMapper.readValue(
                resource.getInputStream(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Person.class)
        );
        this.persons = Collections.unmodifiableList(loaded);
    }

    public List<Person> getPersons() {
        return persons;
    }

    public Optional<Person> findByName(String name) {
        return persons.stream().filter(p -> p.name().equals(name)).findFirst();
    }
}
