package br.com.dv.ntplayersearch.service;

import br.com.dv.ntplayersearch.model.Country;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Getter
@Service
public class MzCountryService {

    private List<Country> countries;
    private final ResourceLoader resourceLoader;

    public MzCountryService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Country>> typeRef = new TypeReference<>() {};
        try {
            Resource resource = resourceLoader.getResource("classpath:countries.json");
            countries = mapper.readValue(resource.getInputStream(), typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read country codes from JSON", e);
        }
    }

}
