package br.com.dv.ntplayersearch.util;

import br.com.dv.ntplayersearch.model.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class PlayerDataParser {

    public Player parsePlayerData(String responseBody) {
        Document doc = Jsoup.parse(responseBody);

        String name = null;
        Elements nameElements = doc.select("span.player_name");
        if (!nameElements.isEmpty()) {
            Element firstElement = nameElements.first();
            if (firstElement != null) {
                name = firstElement.text();
            }
        }

        Integer age = null;
        Elements tableElements = doc.select("table");
        if (!tableElements.isEmpty()) {
            Element firstTableElement = tableElements.first();
            if (firstTableElement != null) {
                Elements trElements = firstTableElement.select("tr");
                if (!trElements.isEmpty()) {
                    Elements ageElements = trElements.get(0).select("strong");
                    if (!ageElements.isEmpty()) {
                        Element firstElement = ageElements.first();
                        if (firstElement != null) {
                            age = Integer.parseInt(firstElement.text());
                        }
                    }
                }
            }
        }

        Long value = null;
        if (!tableElements.isEmpty()) {
            Element firstTableElement = tableElements.first();
            if (firstTableElement != null) {
                Elements trElements = firstTableElement.select("tr");
                if (trElements.size() > 4) {
                    Elements valueElements = trElements.get(4).select("span");
                    if (!valueElements.isEmpty()) {
                        Element firstElement = valueElements.first();
                        if (firstElement != null) {
                            value = Long.parseLong(firstElement.text().replaceAll("[^0-9]", ""));
                        }
                    }
                }
            }
        }

        Integer totalBalls = null;
        if (!tableElements.isEmpty()) {
            Element firstTableElement = tableElements.first();
            if (firstTableElement != null) {
                Elements trElements = firstTableElement.select("tr");
                if (trElements.size() > 6) {
                    Elements totalBallsElements = trElements.get(6).select("span");
                    if (!totalBallsElements.isEmpty()) {
                        Element firstElement = totalBallsElements.first();
                        if (firstElement != null) {
                            totalBalls = Integer.parseInt(firstElement.text());
                        }
                    }
                }
            }
        }

        Elements attributesElements = doc.select("table.player_skills img.skill");

        Integer speed = getSkillValue(attributesElements, 0);
        Integer stamina = getSkillValue(attributesElements, 1);
        Integer playIntelligence = getSkillValue(attributesElements, 2);
        Integer passing = getSkillValue(attributesElements, 3);
        Integer shooting = getSkillValue(attributesElements, 4);
        Integer heading = getSkillValue(attributesElements, 5);
        Integer keeping = getSkillValue(attributesElements, 6);
        Integer ballControl = getSkillValue(attributesElements, 7);
        Integer tackling = getSkillValue(attributesElements, 8);
        Integer aerialPassing = getSkillValue(attributesElements, 9);
        Integer setPlays = getSkillValue(attributesElements, 10);
        Integer experience = getSkillValue(attributesElements, 11);

        return new Player(
                name,
                null,
                null,
                null,
                age,
                value,
                totalBalls,
                speed,
                stamina,
                playIntelligence,
                passing,
                shooting,
                heading,
                keeping,
                ballControl,
                tackling,
                aerialPassing,
                setPlays,
                experience,
                null
        );
    }

    private Integer getSkillValue(Elements elements, int index) {
        Integer skillValue = null;
        if (elements.size() > index) {
            String skillText = elements.get(index).attr("alt").split(": ")[1];
            skillValue = Integer.parseInt(skillText);
        }
        return skillValue;
    }
}
