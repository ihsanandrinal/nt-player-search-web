package br.com.dv.ntplayersearch.service;

import br.com.dv.ntplayersearch.model.Player;
import br.com.dv.ntplayersearch.model.PlayerMinSkills;

public class PlayerEvaluator {

    private final PlayerMinSkills playerMinSkills;

    public PlayerEvaluator(PlayerMinSkills playerMinSkills) {
        this.playerMinSkills = playerMinSkills;
    }

    public boolean isAPlayerThatMeetsTheRequirements(Player player) {
        return player.speed() >= playerMinSkills.speed() &&
                player.stamina() >= playerMinSkills.stamina() &&
                player.playIntelligence() >= playerMinSkills.playIntelligence() &&
                player.passing() >= playerMinSkills.passing() &&
                player.shooting() >= playerMinSkills.shooting() &&
                player.heading() >= playerMinSkills.heading() &&
                player.keeping() >= playerMinSkills.keeping() &&
                player.ballControl() >= playerMinSkills.ballControl() &&
                player.tackling() >= playerMinSkills.tackling() &&
                player.aerialPassing() >= playerMinSkills.aerialPassing() &&
                player.setPlays() >= playerMinSkills.setPlays() &&
                player.experience() >= playerMinSkills.experience() &&
                player.totalBalls() >= playerMinSkills.totalBalls();
    }

}
