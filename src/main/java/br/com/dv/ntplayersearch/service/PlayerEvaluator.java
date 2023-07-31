package br.com.dv.ntplayersearch.service;

import br.com.dv.ntplayersearch.model.Player;
import br.com.dv.ntplayersearch.model.PlayerMinSkills;

public class PlayerEvaluator {

    private final PlayerMinSkills playerMinSkills;

    public PlayerEvaluator(PlayerMinSkills playerMinSkills) {
        this.playerMinSkills = playerMinSkills;
    }

    public boolean isAPlayerThatMeetsTheRequirements(Player player) {
        return player.getSpeed() >= playerMinSkills.speed() &&
                player.getStamina() >= playerMinSkills.stamina() &&
                player.getPlayIntelligence() >= playerMinSkills.playIntelligence() &&
                player.getPassing() >= playerMinSkills.passing() &&
                player.getShooting() >= playerMinSkills.shooting() &&
                player.getHeading() >= playerMinSkills.heading() &&
                player.getKeeping() >= playerMinSkills.keeping() &&
                player.getBallControl() >= playerMinSkills.ballControl() &&
                player.getTackling() >= playerMinSkills.tackling() &&
                player.getAerialPassing() >= playerMinSkills.aerialPassing() &&
                player.getSetPlays() >= playerMinSkills.setPlays() &&
                player.getExperience() >= playerMinSkills.experience() &&
                player.getTotalBalls() >= playerMinSkills.totalBalls();
    }

}
