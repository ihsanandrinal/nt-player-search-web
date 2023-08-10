package br.com.dv.ntplayersearch.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PlayerSearchForm {

    @NotBlank(message = "Please select a country.")
    private String countryCode;

    @Pattern(
            regexp = "https://www\\.managerzone\\.com/ajax\\.php\\?p=nationalTeams&sub=players&ntid=\\d+&cid=\\d+&type=national_team(_u21)?&sport=soccer",
            message = "Invalid Players Tab URL format."
    )
    private String playersTabUrl;

    @NotNull(message = "Initial League ID is required.")
    private Integer initialLeagueId;

    @NotNull(message = "Final League ID is required.")
    private Integer finalLeagueId;

    private Integer minAge;

    private Integer maxAge;

    @Pattern(regexp = "^[a-zA-Z0-9]{26}$", message = "Invalid Session ID format.")
    private String sessionId;

    private PlayerMinSkills playerMinSkills = new PlayerMinSkills();

}