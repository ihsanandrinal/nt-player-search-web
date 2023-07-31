package br.com.dv.ntplayersearch.model;

public record Player(
        String name,
        Long playerId,
        Long teamId,
        String teamName,
        Integer age,
        Long value,
        Integer totalBalls,
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
        String ntPos
) {
}
