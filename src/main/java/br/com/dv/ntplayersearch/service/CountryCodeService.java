package br.com.dv.ntplayersearch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CountryCodeService {

    private final ResourceLoader resourceLoader;
    @Getter
    private Map<String, String> countryCodes;

    public CountryCodeService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, String>> typeRef = new TypeReference<>() {};
        try {
            Resource resource = resourceLoader.getResource("classpath:mz_country_codes.json");
            countryCodes = mapper.readValue(resource.getInputStream(), typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read country codes from JSON", e);
        }
    }

}
