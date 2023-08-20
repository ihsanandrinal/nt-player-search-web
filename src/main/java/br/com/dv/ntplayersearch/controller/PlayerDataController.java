package br.com.dv.ntplayersearch.controller;

import br.com.dv.ntplayersearch.model.Country;
import br.com.dv.ntplayersearch.model.Player;
import br.com.dv.ntplayersearch.model.PlayerSearchForm;
import br.com.dv.ntplayersearch.model.PlayerSearchRequest;
import br.com.dv.ntplayersearch.service.MzCountryService;
import br.com.dv.ntplayersearch.service.PlayerDataService;
import br.com.dv.ntplayersearch.service.RateLimiterService;
import br.com.dv.ntplayersearch.service.SearchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Controller
public class PlayerDataController {

    private final PlayerDataService playerDataService;
    private final MzCountryService mzCountryService;
    private final SearchService searchService;
    private final RateLimiterService rateLimiterService;

    public PlayerDataController(
            PlayerDataService playerDataService,
            MzCountryService mzCountryService,
            SearchService searchService,
            RateLimiterService rateLimiterService
    ) {
        this.playerDataService = playerDataService;
        this.mzCountryService = mzCountryService;
        this.searchService = searchService;
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("form", new PlayerSearchForm());
        return "index";
    }

    @PostMapping("/search")
    public String search(
            @Valid @ModelAttribute("form") PlayerSearchForm form,
            BindingResult bindingResult,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        if (!rateLimiterService.isAllowed(request.getRemoteAddr())) {
            return "ratelimitexceeded";
        }

        Country selectedCountry = mzCountryService.getCountries().stream()
                .filter(country -> country.code().equals(form.getCountry()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid country code!"));

        String searchId = UUID.randomUUID().toString();

        CompletableFuture.runAsync(() -> {
            try {
                playerDataService.getPlayers(new PlayerSearchRequest(form, selectedCountry), searchId);
            } catch (Exception e) {
                log.error("Error while searching players", e);
            }
        });

        return "redirect:/progress/" + searchId;
    }

    @GetMapping("/progress/{searchId}")
    public String progress(@PathVariable String searchId, Model model) {
        model.addAttribute("searchId", searchId);
        return "progress";
    }

    @GetMapping("/results/{searchId}")
    public String results(@PathVariable String searchId, Model model) {
        List<Player> players = searchService.getResults(searchId);

        if (players == null) {
            return "errorpage";
        }

        model.addAttribute("players", players);

        return "results";
    }

    @ModelAttribute("countries")
    public List<Country> getCountries() {
        return mzCountryService.getCountries();
    }

}