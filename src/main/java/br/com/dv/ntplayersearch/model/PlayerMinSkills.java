package br.com.dv.ntplayersearch.model;

public record PlayerMinSkills(
        Integer speed,
        Integer stamina,
        Integer playIntelligence,
        Integer passing,
        Integer shooting,
        Integer heading,
        Integer keeping,
        Integer ballControl,
        Integer tackling,
        Integer aerialPassing,
        Integer setPlays,
        Integer experience,
        Integer totalBalls
) {
}
