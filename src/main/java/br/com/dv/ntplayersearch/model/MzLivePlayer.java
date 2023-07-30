package br.com.dv.ntplayersearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MzLivePlayer(
        Integer pos,
        Long id,
        String country,
        String name,
        Integer age,
        @JsonProperty("team_id")
        Long teamId,
        @JsonProperty("team_name")
        String teamName,
        @JsonProperty("team_country")
        String teamCountry,
        Long value
) {
}
