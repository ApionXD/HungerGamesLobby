package com.apion.hglobby.matchmake;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hglobby.bungee.BungeeMessageExecutor;
import com.apion.hglobby.bungee.BungeeMessageListener;
import com.apion.hglobby.server.HungeeServerExecutor;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class QueueManager {
    private static final Logger logger = Bukkit.getLogger();
    //List of players in queue
    private List<String> playerList;
    private Map<String, Integer> playerCountMap;

    public QueueManager() {
        playerList = new ArrayList<>();
    }


    //This method is a lil complex
    // 1. Get names of all servers
    // 2. Get player counts for each server
    // 3. Store player counts in playerCountMap
    // 4. Once we have all player counts, pick the optimal server
    @SuppressWarnings("unchecked")
    public void createArena() {
        final HungerGamesLobby instance = HungerGamesLobby.getInstance();

        //The player we send the message to doesn't matter, Bungee intercepts it before it arrives

        //Get list of servers
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
