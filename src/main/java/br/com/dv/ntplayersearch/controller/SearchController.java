package br.com.dv.ntplayersearch.controller;

import br.com.dv.ntplayersearch.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/logs/{searchId}")
    public String logs(@PathVariable String searchId) {
        return searchService.getLogs(searchId);
    }

    @GetMapping("/status/{searchId}")
    public Map<String, Boolean> status(@PathVariable String searchId) {
        return Collections.singletonMap("isComplete", searchService.isSearchComplete(searchId));
    }

}
