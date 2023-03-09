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
import java.util.logging.Logger;

public class CreateArenaRunnable extends BukkitRunnable {
    private Map<String, Integer> playerCountMap;
    final Logger logger = Bukkit.getLogger();
    final Queue<UUID> players;
    final NamespacedKey bossBarKey;
    public CreateArenaRunnable(final Queue<UUID> players, final NamespacedKey bossBarKey) {
        this.players = players;
        this.bossBarKey = bossBarKey;
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

        logger.info(MessageFormat.format("Starting Arena with {0} players from {1} server", players.size(), Bukkit.getServer().getName()));
        BungeeMessageExecutor.getServerList().whenComplete(
                (servers, throwable) -> {
                    final List<String> serverList = (List<String>) servers;
                    for (final String server : serverList) {
                        BungeeMessageExecutor.getPlayerCountForServer(server).whenComplete(
                                (count, throwable1) -> {
                                    logger.warning("Got all info");
                                    final Pair<String, Integer> serverNameCountPair = (Pair<String, Integer>) count;
                                    playerCountMap.put(server, serverNameCountPair.getRight());
                                    HungeeServerExecutor serverMessager = HungerGamesLobby.hungeeServerExecutor;
                                    serverMessager.sendInitArenaMessage(serverNameCountPair.getLeft());
                                }
                        );
                    }
                }
        );
    }
}
