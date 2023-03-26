package com.apion.hglobby.runnables;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hglobby.bungee.BungeeMessageExecutor;
import com.apion.hglobby.server.HungeeServerExecutor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class CreateArenaRunnable extends BukkitRunnable {
    private Map<String, Integer> playerCountMap;
    final Logger logger = Bukkit.getLogger();
    final Queue<UUID> players;
    final int delay;
    final NamespacedKey bossBarKey;
    public CreateArenaRunnable(final Queue<UUID> players, final NamespacedKey bossBarKey, int delay) {
        this.players = players;
        this.bossBarKey = bossBarKey;
        playerCountMap = new HashMap<String, Integer>();
        this.delay = delay;
    }

    // 1. Get names of all servers
    // 2. Get player counts for each server
    // 3. Store player counts in playerCountMap
    // 4. Once we have all player counts, pick the optimal server
    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        final BossBar queueBossBar = Bukkit.getBossBar(bossBarKey);

        if (queueBossBar != null) {
            queueBossBar.removeAll();
            Bukkit.removeBossBar(bossBarKey);
        }

        BungeeMessageExecutor.getServerList().whenComplete(
                (servers, throwable) -> {
                    final List<String> serverList = new ArrayList<>((List<String>) servers);
                    logger.warning(String.valueOf(serverList.size()));
                    serverList.remove(HungerGamesLobby.getInstance().getStringFromConfig("bungee.serverName"));
                    logger.warning("Here");
                    final CompletableFuture<Object>[] playerCountRequests = new CompletableFuture[serverList.size()];
                    for (int i = 0; i < serverList.size(); i++) {
                        CompletableFuture<Object> playerCountRequest = BungeeMessageExecutor.getPlayerCountForServer(serverList.get(i));
                        playerCountRequests[i] = playerCountRequest;
                        playerCountRequest.whenComplete(
                                (count, throwable1) -> {
                                    final Pair<String, Integer> serverNameCountPair = (Pair<String, Integer>) count;
                                    logger.warning(MessageFormat.format("Got player count for server {0}: {1}", serverNameCountPair.getLeft(), serverNameCountPair.getRight()));
                                    playerCountMap.put(serverNameCountPair.getLeft(), serverNameCountPair.getRight());
                                }
                        );
                    }
                    CompletableFuture.allOf(playerCountRequests).whenComplete((f,e) -> {
                        String serverName = playerCountMap.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
                        logger.warning(String.format("Sending arena initialization msg to %s", serverName));
                        HungeeServerExecutor serverMessager = HungerGamesLobby.hungeeServerExecutor;
                        serverMessager.sendInitArenaMessage(serverName);
                        new MovePlayersRunnable(players, bossBarKey, serverName).runTaskLater(
                                HungerGamesLobby.getInstance(),
                                delay
                        );
                    });
                }
        );
    }
}
