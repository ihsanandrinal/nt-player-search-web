package br.com.dv.ntplayersearch.model;

import lombok.Data;

@Data
public class Player {
    private String name;
    private Long playerId;
    private Long teamId;
    private String teamName;
    private Integer age;
    private Long value;
    private Integer totalBalls;
    private Integer speed;
    private Integer stamina;
    private Integer playIntelligence;
    private Integer passing;
    private Integer shooting;
    private Integer heading;
    private Integer keeping;
    private Integer ballControl;
    private Integer tackling;
    private Integer aerialPassing;
    private Integer setPlays;
    private Integer experience;
    private String ntPos;

    public String toHtml(String ball) {
        // TODO: implement
        return "";
    }
}
