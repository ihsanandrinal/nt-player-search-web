package br.com.dv.ntplayersearch.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PlayerMinSkills {

    @Min(0) @Max(10)
    private Integer speed = 0;

    @Min(0) @Max(10)
    private Integer stamina = 0;

    @Min(0) @Max(10)
    private Integer playIntelligence = 0;

    @Min(0) @Max(10)
    private Integer passing = 0;

    @Min(0) @Max(10)
    private Integer shooting = 0;

    @Min(0) @Max(10)
    private Integer heading = 0;

    @Min(0) @Max(10)
    private Integer keeping = 0;

    @Min(0) @Max(10)
    private Integer ballControl = 0;

    @Min(0) @Max(10)
    private Integer tackling = 0;

    @Min(0) @Max(10)
    private Integer aerialPassing = 0;

    @Min(0) @Max(10)
    private Integer setPlays = 0;

    @Min(0) @Max(10)
    private Integer experience = 0;

    @Min(0) @Max(100)
    private Integer totalBalls = 0;

}
