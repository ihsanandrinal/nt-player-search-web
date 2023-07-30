package br.com.dv.ntplayersearch.model;

import lombok.Data;

@Data
public class PlayerSearchForm {
    private String country;
    private String url;
    private String sessionId;
    private Integer startingLeagueId;
    private Integer endingLeagueId;
    private Integer minAge;
    private Integer maxAge;
    private PlayerMinSkills playerMinSkills;
}
