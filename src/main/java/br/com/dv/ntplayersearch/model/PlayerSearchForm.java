package br.com.dv.ntplayersearch.model;

import lombok.Data;

@Data
public class PlayerSearchForm {
    private String countryCode;
    private String playersTabUrl;
    private String sessionId;
    private Integer initialLeagueId;
    private Integer finalLeagueId;
    private Integer minAge;
    private Integer maxAge;
    private PlayerMinSkills playerMinSkills = new PlayerMinSkills();
}
