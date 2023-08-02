package br.com.dv.ntplayersearch.service;

import br.com.dv.ntplayersearch.model.Player;
import br.com.dv.ntplayersearch.model.PlayerMinSkills;

public class PlayerEvaluator {

    private final PlayerMinSkills playerMinSkills;

    public PlayerEvaluator(PlayerMinSkills playerMinSkills) {
        this.playerMinSkills = playerMinSkills;
    }

    public boolean isAPlayerThatMeetsTheRequirements(Player player) {
        return player.speed() >= playerMinSkills.getSpeed() &&
                player.stamina() >= playerMinSkills.getStamina() &&
                player.playIntelligence() >= playerMinSkills.getPlayIntelligence() &&
                player.passing() >= playerMinSkills.getPassing() &&
                player.shooting() >= playerMinSkills.getShooting() &&
                player.heading() >= playerMinSkills.getHeading() &&
                player.keeping() >= playerMinSkills.getKeeping() &&
                player.ballControl() >= playerMinSkills.getBallControl() &&
                player.tackling() >= playerMinSkills.getTackling() &&
                player.aerialPassing() >= playerMinSkills.getAerialPassing() &&
                player.setPlays() >= playerMinSkills.getSetPlays() &&
                player.experience() >= playerMinSkills.getExperience() &&
                player.totalBalls() >= playerMinSkills.getTotalBalls();
    }

}
