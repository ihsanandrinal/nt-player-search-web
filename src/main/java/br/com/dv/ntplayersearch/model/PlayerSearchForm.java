package br.com.dv.ntplayersearch.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PlayerSearchForm {

    @NotBlank(message = "Please select a country.")
    private String country;

    private Integer minAge;

    private Integer maxAge;

    @Pattern(regexp = "^[a-zA-Z0-9]{26}$", message = "Invalid Session ID format.")
    private String sessionId;

    private PlayerMinSkills playerMinSkills = new PlayerMinSkills();

}