package br.com.dv.ntplayersearch.controller;

import br.com.dv.ntplayersearch.model.Player;
import br.com.dv.ntplayersearch.model.PlayerSearchForm;
import br.com.dv.ntplayersearch.service.CountryCodeService;
import br.com.dv.ntplayersearch.service.PlayerDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class PlayerDataController {

    private final PlayerDataService playerDataService;
    private final CountryCodeService countryCodeService;

    public PlayerDataController(PlayerDataService playerDataService, CountryCodeService countryCodeService) {
        this.playerDataService = playerDataService;
        this.countryCodeService = countryCodeService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("form", new PlayerSearchForm());
        return "index";
    }

    @PostMapping("/search")
    public String search(PlayerSearchForm form, Model model) {
        try {
            List<Player> players = playerDataService.getPlayers(form);
            model.addAttribute("players", players);
        } catch (Exception e) {
            log.error("Error occurred while searching for players", e);
        }
        return "results";
    }

    @ModelAttribute("countryCodes")
    public Map<String, String> getCountryCodes() {
        return countryCodeService.getCountryCodes();
    }

}
