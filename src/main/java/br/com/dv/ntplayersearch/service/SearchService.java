package br.com.dv.ntplayersearch.service;

import br.com.dv.ntplayersearch.model.Player;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SearchService {

    private final Map<String, List<String>> logMessages = new ConcurrentHashMap<>();
    private final Map<String, List<Player>> searchResults = new ConcurrentHashMap<>();
    private final Map<String, Boolean> searchStatus = new ConcurrentHashMap<>();
    private final Map<String, Long> creationTimes = new ConcurrentHashMap<>();

    public void appendLog(String searchId, String message) {
        logMessages.computeIfAbsent(searchId, k -> new ArrayList<>()).add(message);
    }

    public String getLogs(String searchId) {
        return String.join("<br>", logMessages.getOrDefault(searchId, Collections.emptyList()));
    }

    public void storeResults(String searchId, List<Player> results) {
        searchResults.put(searchId, results);
        creationTimes.put(searchId, System.currentTimeMillis());
    }

    public List<Player> getResults(String searchId) {
        return searchResults.get(searchId);
    }

    public void setSearchStatus(String searchId, boolean isComplete) {
        searchStatus.put(searchId, isComplete);
    }

    public boolean isSearchComplete(String searchId) {
        return searchStatus.getOrDefault(searchId, false);
    }

    @Scheduled(fixedRate = 3600000)
    public void clearOldSearchData() {
        long currentTime = System.currentTimeMillis();
        creationTimes.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > 24 * 60 * 60 * 1000) {
                String searchId = entry.getKey();
                logMessages.remove(searchId);
                searchResults.remove(searchId);
                searchStatus.remove(searchId);
                return true;
            }
            return false;
        });
    }

}