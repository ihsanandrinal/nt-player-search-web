package br.com.dv.ntplayersearch.service;

import br.com.dv.ntplayersearch.model.Player;
import br.com.dv.ntplayersearch.model.PlayerInfo;
import br.com.dv.ntplayersearch.model.PlayerSearchForm;
import br.com.dv.ntplayersearch.util.PlayerDataParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.xml.parsers.DocumentBuilder;
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

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final DocumentBuilder documentBuilder;
    private final PlayerDataParser playerDataParser;
    private final Integer threadPoolSize;

    public PlayerDataService(
            WebClient webClient,
            ObjectMapper objectMapper,
            DocumentBuilder documentBuilder,
            PlayerDataParser playerDataParser,
            @Value("${threadPoolSize}")
            Integer threadPoolSize
    ) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.documentBuilder = documentBuilder;
        this.playerDataParser = playerDataParser;
        this.threadPoolSize = threadPoolSize;
    }

    public List<Player> getPlayers(PlayerSearchForm form) {
        PlayerEvaluator playerEvaluator = new PlayerEvaluator(form.getPlayerMinSkills());

        DataFetcher dataFetcher = new DataFetcher(
                form.getCountry(),
                form.getUrl().replace("sub=players", "sub=search&pid="),
                form.getSessionId(),
                IntStream.rangeClosed(form.getStartingLeagueId(), form.getEndingLeagueId()).boxed().toList(),
                IntStream.rangeClosed(form.getMinAge(), form.getMaxAge()).boxed().toList(),
                playerDataParser,
                playerEvaluator,
                webClient,
                objectMapper,
                documentBuilder,
                threadPoolSize
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

        return dataFetcher.getCompliantPlayers();
    }

}
