package br.com.dv.ntplayersearch.service;

import br.com.dv.ntplayersearch.model.Player;
import br.com.dv.ntplayersearch.model.PlayerInfo;
import br.com.dv.ntplayersearch.model.PlayerSearchForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

@Slf4j
@Service
public class PlayerDataService {

    private final PlayerDataParser playerDataParser;
    private PlayerEvaluator playerEvaluator;
    private WebDataFetcher webDataFetcher;

    public PlayerDataService() {
        this.playerDataParser = new PlayerDataParser();
    }

    public List<Player> getPlayers(PlayerSearchForm form) {
        this.playerEvaluator = new PlayerEvaluator(form.getPlayerMinSkills());
        this.webDataFetcher = new WebDataFetcher(
                form.getCountry(),
                form.getUrl(),
                form.getSessionId(),
                IntStream.rangeClosed(form.getStartingLeagueId(), form.getEndingLeagueId()).boxed().toList(),
                IntStream.rangeClosed(form.getMinAge(), form.getMaxAge()).boxed().toList(),
                playerDataParser,
                playerEvaluator
        );

        List<String> allTeamIds = webDataFetcher.getTeamIds();
        Map<Long, PlayerInfo> thePlayers = webDataFetcher.getPlayersFromTeamIds(allTeamIds);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();

        for (Map.Entry<Long, PlayerInfo> entry : thePlayers.entrySet()) {
            Long pid = entry.getKey();
            PlayerInfo pInfo = entry.getValue();
            futures.add(executor.submit(() -> webDataFetcher.checkPlayer(pid, pInfo)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing player data", e);
            }
        }
        executor.shutdown();

        // TODO: return the results
        return List.of();
    }

}
