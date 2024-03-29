package com.apion.hglobby.runnables;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hglobby.bungee.BungeeMessageExecutor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Futures {
    private final Logger logger = Bukkit.getLogger();
    /**
     * Gets the server with the least amount of players on it.
     * @return CompletableFuture that will return the server with the lowest amount of players on it.
     */
    @SuppressWarnings("unchecked")
    public CompletableFuture<String> getServerWithMinPlayers() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        BungeeMessageExecutor.getServerList().whenComplete(
                (servers, throwable) -> {
                    final Map<String, Integer> playerCountMap = new HashMap<>();

                    final List<String> serverList = new ArrayList<>((List<String>) servers);
                    logger.info("serverList size: " + serverList.size());
                    // Remove self from list
                    serverList.remove(HungerGamesLobby.getInstance().getStringFromConfig("bungee.serverName"));
                    // Get player count for each server
                    final CompletableFuture<Object>[] playerCountRequests = new CompletableFuture[serverList.size()];
                    for (int i = 0; i < serverList.size(); i++) {
                        CompletableFuture<Object> playerCountRequest = BungeeMessageExecutor.getPlayerCountForServer(serverList.get(i));
                        playerCountRequests[i] = playerCountRequest.whenComplete(
                                (count, throwable1) -> {
                                    final Pair<String, Integer> serverNameCountPair = (Pair<String, Integer>) count;
                                    logger.info(MessageFormat.format("Got player count for server {0}: {1}", serverNameCountPair.getLeft(), serverNameCountPair.getRight()));
                                    playerCountMap.put(serverNameCountPair.getLeft(), serverNameCountPair.getRight());
                                }
                        );
                    }

                    final CompletableFuture<Void> playerCountDone = CompletableFuture.allOf(playerCountRequests);
                    playerCountDone.whenComplete(
                            (f, e) -> {
                                final int minPlayerCount = playerCountMap.entrySet()
                                        .stream()
                                        .min(Map.Entry.comparingByValue())
                                        .orElseThrow(() -> {
                                            logger.severe("There were no servers to get the min players?");
                                            return new IllegalStateException();
                                        }).getValue();

                                List<String> serversWithMinCount = playerCountMap
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> entry.getValue() == minPlayerCount)
                                        .map(Map.Entry::getKey)
                                        .toList();
                                Random random = new Random();
                                String server = serversWithMinCount.get(random.nextInt(serversWithMinCount.size()));

                                future.complete(server);
                            }
                    );
                }
        );
        return future;
    }
}
