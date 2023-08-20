package br.com.dv.ntplayersearch.controller;

import br.com.dv.ntplayersearch.model.Country;
import br.com.dv.ntplayersearch.model.Player;
import br.com.dv.ntplayersearch.model.PlayerSearchForm;
import br.com.dv.ntplayersearch.model.PlayerSearchRequest;
import br.com.dv.ntplayersearch.service.MzCountryService;
import br.com.dv.ntplayersearch.service.PlayerDataService;
import br.com.dv.ntplayersearch.service.SearchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Controller
public class PlayerDataController {

    private final PlayerDataService playerDataService;
    private final MzCountryService mzCountryService;
    private final SearchService searchService;

    public PlayerDataController(
            PlayerDataService playerDataService,
            MzCountryService mzCountryService,
            SearchService searchService
    ) {
        this.playerDataService = playerDataService;
        this.mzCountryService = mzCountryService;
        this.searchService = searchService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("form", new PlayerSearchForm());
        return "index";
    }

    @PostMapping("/search")
    public String search(@Valid @ModelAttribute("form") PlayerSearchForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "index";
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
    public ModelAndView progress(@PathVariable String searchId, ModelAndView modelAndView) {
        modelAndView.addObject("searchId", searchId);
        modelAndView.setViewName("progress");
        return modelAndView;
    }

    @GetMapping("/logs/{searchId}")
    @ResponseBody
    public String logs(@PathVariable String searchId) {
        return searchService.getLogs(searchId);
    }

    @GetMapping("/status/{searchId}")
    @ResponseBody
    public Map<String, Boolean> status(@PathVariable String searchId) {
        return Collections.singletonMap("isComplete", searchService.isSearchComplete(searchId));
    }

    @GetMapping("/results/{searchId}")
    public ModelAndView results(@PathVariable String searchId) {
        List<Player> players = searchService.getResults(searchId);

        if (players == null) {
            return new ModelAndView("errorpage");
        }

        ModelAndView modelAndView = new ModelAndView("results");
        modelAndView.addObject("players", players);

        return modelAndView;
    }

    @ModelAttribute("countries")
    public List<Country> getCountries() {
        return mzCountryService.getCountries();
    }

}