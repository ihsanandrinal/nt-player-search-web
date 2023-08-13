package br.com.dv.ntplayersearch.service;

import br.com.dv.ntplayersearch.model.Player;
import br.com.dv.ntplayersearch.model.PlayerInfo;
import br.com.dv.ntplayersearch.model.PlayerSearchRequest;
import br.com.dv.ntplayersearch.util.PlayerDataParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

@Slf4j
@Service
public class PlayerDataService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final PlayerDataParser playerDataParser;
    private final Integer threadPoolSize;
    private final SearchService searchService;

    public PlayerDataService(
            WebClient webClient,
            ObjectMapper objectMapper,
            PlayerDataParser playerDataParser,
            @Value("${threadPoolSize}") Integer threadPoolSize,
            SearchService searchService
    ) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.playerDataParser = playerDataParser;
        this.threadPoolSize = threadPoolSize;
        this.searchService = searchService;
    }

    public void getPlayers(PlayerSearchRequest request, String searchId) {
        searchService.appendLog(searchId, "Starting search");

        DataFetcher dataFetcher = new DataFetcher(
                request.selectedCountry().code(),
                request.selectedCountry().ntid(),
                request.selectedCountry().cid(),
                IntStream.rangeClosed(request.form().getMinAge(), request.form().getMaxAge()).boxed().toList(),
                request.form().getSessionId(),
                new PlayerEvaluator(request.form().getPlayerMinSkills()),
                playerDataParser,
                webClient,
                objectMapper,
                threadPoolSize,
                searchId,
                searchService
        );

        List<String> allTeamIds = dataFetcher.getTeamIds();
        Map<String, PlayerInfo> thePlayers = dataFetcher.getPlayersFromTeamIds(allTeamIds);

        ExecutorService executor = Executors.newFixedThreadPool(dataFetcher.getThreadPoolSize());
        List<Future<?>> futures = new ArrayList<>();

        for (Map.Entry<String, PlayerInfo> entry : thePlayers.entrySet()) {
            String pid = entry.getKey();
            PlayerInfo pInfo = entry.getValue();
            futures.add(executor.submit(() -> dataFetcher.checkPlayer(pid, pInfo)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing player data", e);
            }
        }
        executor.shutdown();

        var results = dataFetcher.getCompliantPlayers()
                .stream()
                .sorted(Comparator.comparing(Player::totalBalls).reversed())
                .toList();

        searchService.appendLog(searchId, "Done!");
        searchService.storeResults(searchId, results);
        searchService.setSearchStatus(searchId, true);
    }

}
