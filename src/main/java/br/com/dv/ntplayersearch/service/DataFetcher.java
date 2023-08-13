package br.com.dv.ntplayersearch.service;

import br.com.dv.ntplayersearch.model.*;
import br.com.dv.ntplayersearch.util.PlayerDataParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
public class DataFetcher {

    private final String country;
    private final Integer ntid;
    private final Integer cid;
    private final List<Integer> ages;
    private final String sessionId;
    private final PlayerEvaluator playerEvaluator;
    private final PlayerDataParser playerDataParser;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Integer threadPoolSize;
    private final List<Player> compliantPlayers;
    private final AtomicInteger atomicInteger;
    private final String searchId;
    private final SearchService searchService;

    public DataFetcher(
            String country,
            Integer ntid,
            Integer cid,
            List<Integer> ages,
            String sessionId,
            PlayerEvaluator playerEvaluator,
            PlayerDataParser playerDataParser,
            WebClient webClient,
            ObjectMapper objectMapper,
            Integer threadPoolSize,
            String searchId,
            SearchService searchService
    ) {
        this.country = country;
        this.ntid = ntid;
        this.cid = cid;
        this.ages = ages;
        this.sessionId = sessionId;
        this.playerEvaluator = playerEvaluator;
        this.playerDataParser = playerDataParser;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.threadPoolSize = threadPoolSize;
        this.compliantPlayers = new CopyOnWriteArrayList<>();
        this.atomicInteger = new AtomicInteger(0);
        this.searchId = searchId;
        this.searchService = searchService;
    }

    public List<String> getTeamIds() {
        Set<String> teamIds = new ConcurrentSkipListSet<>();

        getTeamIdsFromHomeCountrySeniorLeagues(teamIds);

        // (No reason to check teams from other countries if the players are under 18)
        if (ages.get(ages.size() - 1) > 18) {
            getAdditionalTeamIdsFromTopTeams(teamIds, "senior_teams.json");
            getAdditionalTeamIdsFromTopTeams(teamIds, "uxx_teams.json");
            getAdditionalTeamIdsFromMzLive(teamIds, "senior", "players");
            getAdditionalTeamIdsFromMzLive(teamIds, "senior", "teams");
            getAdditionalTeamIdsFromMzLive(teamIds, "U18", "players");
            getAdditionalTeamIdsFromMzLive(teamIds, "U18", "teams");
            getAdditionalTeamIdsFromMzLive(teamIds, "U21", "players");
            getAdditionalTeamIdsFromMzLive(teamIds, "U21", "teams");
            getAdditionalTeamIdsFromMzLive(teamIds, "U23", "players");
            getAdditionalTeamIdsFromMzLive(teamIds, "U23", "teams");
        }

        return new ArrayList<>(teamIds);
    }

    private void getTeamIdsFromHomeCountrySeniorLeagues(Set<String> teamIds) {
        searchService.appendLog(searchId, "Getting home country team ids…");

        List<Integer> homeCountryLeagueIds = getHomeCountrySeniorLeagueIdsFromMzLive();

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        for (Integer leagueId : homeCountryLeagueIds) {
            executor.submit(() -> {
                try {
                    String url = "https://www.managerzone.com/xml/team_league.php?sport_id=1&league_id=" + leagueId;
                    String response = webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    List<String> leagueTeamIds = extractTeamIdsFromXml(response);
                    teamIds.addAll(leagueTeamIds);
                } catch (Exception e) {
                    log.error("Error while getting team ids for league " + leagueId, e);
                }
            });
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
                log.warn("Executor did not terminate in the specified time.");
            }
        } catch (InterruptedException e) {
            log.error("Executor was interrupted.", e);
        }
    }

    private List<Integer> getHomeCountrySeniorLeagueIdsFromMzLive() {
        List<Integer> leagueIds = new ArrayList<>();

        String url = "https://mzlive.eu/mzlive.php?action=list&type=top100&mode=leagues&country=" + country;
        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(response);
            JsonNode leaguesNode = rootNode.get("leagues");
            if (leaguesNode.isArray()) {
                for (JsonNode leagueNode : leaguesNode) {
                    leagueIds.add(leagueNode.get("id").asInt());
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error during JSON processing", e);
        }

        return leagueIds;
    }

    private List<String> extractTeamIdsFromXml(String xml) {
        List<String> teamIds = new ArrayList<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("Team");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    teamIds.add(element.getAttribute("teamId"));
                }
            }
        } catch (Exception e) {
            log.error("Error while extracting team ids from XML", e);
        }

        return teamIds;
    }

    private void getAdditionalTeamIdsFromTopTeams(Set<String> teamIds, String fileName) {
        searchService.appendLog(searchId,
                "Getting additional team ids from top " + fileName.split("_teams")[0] + " teams…");

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

        if (inputStream != null) {
            try {
                TeamIdCollection teamIdsFromFile = objectMapper.readValue(inputStream, TeamIdCollection.class);
                teamIds.addAll(teamIdsFromFile.teamIds());
            } catch (IOException e) {
                log.error("Error while reading team ids from file " + fileName, e);
            }
        } else {
            log.warn("Could not find file " + fileName);
        }
    }

    private void getAdditionalTeamIdsFromMzLive(Set<String> teamIds, String ageGroup, String mzLiveMode) {
        searchService.appendLog(
                searchId,
                "Getting additional team ids from most valuable " + ageGroup + " " + mzLiveMode + " (MZ Live)…"
        );

        String url = "https://mzlive.eu/mzlive.php?action=list&type=top100&mode=" + mzLiveMode + "&country=" + country;
        if (!ageGroup.equalsIgnoreCase("Senior")) {
            url += "&age=" + ageGroup;
        }

        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get(mzLiveMode.equals("teams") ? "teams" : "players");

            if (itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    if (mzLiveMode.equals("teams")) {
                        String teamId = String.valueOf(itemNode.get("id").asInt());
                        if (!itemNode.get("country").asText().equals(country)) {
                            teamIds.add(teamId);
                        }
                    } else if (mzLiveMode.equals("players")) {
                        String teamId = String.valueOf(itemNode.get("team_id").asInt());
                        teamIds.add(teamId);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error while reading team ids from MZ Live", e);
        }
    }

    public Map<String, PlayerInfo> getPlayersFromTeamIds(List<String> teamIds) {
        searchService.appendLog(searchId, "Trying to find players in " + teamIds.size() + " teams…");

        Map<String, PlayerInfo> playerTeamMap = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        for (String teamId : teamIds) {
            executor.submit(() -> {
                try {
                    searchService.appendLog(searchId, "Checking team " + teamId);

                    String url = "https://www.managerzone.com/xml/team_playerlist.php?sport_id=1&team_id=" + teamId;
                    String xmlContent = webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    Map<String, PlayerInfo> teamPlayers = extractPlayersFromXml(xmlContent, country, ages);
                    playerTeamMap.putAll(teamPlayers);
                } catch (Exception e) {
                    log.error("Error while getting players for team " + teamId, e);
                }
            });
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
                log.warn("Executor did not terminate in the specified time.");
            }
        } catch (InterruptedException e) {
            log.error("Executor was interrupted.", e);
        }

        searchService.appendLog(searchId, playerTeamMap.size() + " players were found and will be checked…");

        return playerTeamMap;
    }

    private Map<String, PlayerInfo> extractPlayersFromXml(String xml, String country, List<Integer> ageRange) {
        Map<String, PlayerInfo> playerTeamMap = new ConcurrentHashMap<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("TeamPlayers");

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String teamId = element.getAttribute("teamId");
                    String teamName = element.getAttribute("teamName");
                    NodeList playerList = element.getElementsByTagName("Player");
                    for (int j = 0; j < playerList.getLength(); ++j) {
                        Node playerNode = playerList.item(j);
                        if (playerNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element playerElement = (Element) playerNode;
                            String countryShortName = playerElement.getAttribute("countryShortname");
                            int playerAge = Integer.parseInt(playerElement.getAttribute("age"));
                            if (countryShortName.equalsIgnoreCase(country) && ageRange.contains(playerAge)) {
                                String playerId = playerElement.getAttribute("id");
                                PlayerInfo playerInfo = new PlayerInfo(playerId, teamId, teamName);
                                playerTeamMap.put(playerInfo.playerId(), playerInfo);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while extracting players from XML", e);
        }

        return playerTeamMap;
    }

    public void checkPlayer(String pid, PlayerInfo pInfo) {
        String playerSearchUrl = "https://www.managerzone.com/ajax.php?p=nationalTeams&sub=search&ntid=&cid=&type=national_team&pid=&sport=soccer"
                .replace("ntid=", "ntid=" + ntid)
                .replace("cid=", "cid=" + cid)
                .replace("pid=", "pid=" + pid);

        try {
            String responseBody = webClient.get()
                    .uri(playerSearchUrl)
                    .header("Cookie", "PHPSESSID=" + sessionId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Player player = playerDataParser.parsePlayerData(responseBody);

            if (isAPlayerThatMeetsTheRequirements(player)) {
                player = getCompletePlayer(player, pid, pInfo);
                compliantPlayers.add(player);
                searchService.appendLog(
                        searchId,
                        String.format(
                                "[%d] - Player %s (%d) from team %s meets the requirements!",
                                atomicInteger.incrementAndGet(), player.name(), player.playerId(), player.teamName()
                        )
                );
            } else {
                atomicInteger.incrementAndGet();
            }
        } catch (Exception e) {
            log.error("Error while checking player " + pid, e);
        }
    }

    private Player getCompletePlayer(Player player, String playerId, PlayerInfo playerInfo) {
        return new Player(
                player.name(),
                Long.parseLong(playerId),
                Long.parseLong(playerInfo.teamId()),
                playerInfo.teamName(),
                player.age(),
                player.value(),
                player.totalBalls(),
                player.speed(),
                player.stamina(),
                player.playIntelligence(),
                player.passing(),
                player.shooting(),
                player.heading(),
                player.keeping(),
                player.ballControl(),
                player.tackling(),
                player.aerialPassing(),
                player.setPlays(),
                player.experience(),
                player.ntPos()
        );
    }

    private boolean isAPlayerThatMeetsTheRequirements(Player player) {
        return playerEvaluator.isAPlayerThatMeetsTheRequirements(player);
    }

}
