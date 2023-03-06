package com.apion.hglobby.matchmake;

import com.apion.hglobby.HungerGamesLobby;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
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
    private List<CompletableFuture<String>> getServersTasks;
    //Maps a server name to a list of tasks that require the server name and number of ppl on the server
    private HashMap<String, List<CompletableFuture<Pair<String,Integer>>>> playerCountTasks;
    private Map<String, Integer> playerCountMap;

    public QueueManager() {
        playerList = new ArrayList<>();
        getServersTasks = new ArrayList<>();
        playerCountTasks = new HashMap<>();
    }

    //This method is a lil complex
    // 1. Get names of all servers
    // 2. Get player counts for each server
    // 3. Store player counts in playerCountMap
    // 4. Once we have all player counts, pick the optimal server
    public void createArena() {
        CompletableFuture<String> future = new CompletableFuture<>();
        ByteArrayDataOutput message = ByteStreams.newDataOutput();
        message.writeUTF("GetServers");
        //The player we send the message to doesnt matter, Bungee intercepts it before it arrives
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        //Get list of servers
        player.sendPluginMessage(null, HungerGamesLobby.BUNGEE_CHANNEL_NAME, message.toByteArray());
        getServersTasks.add(future);
        future.whenComplete((serverList, ex) -> {
            ArrayList<String> servers = Lists.newArrayList(serverList.split(", "));
            ArrayList<CompletableFuture<Pair<String, Integer>>> playerListsToWaitFor = new ArrayList<>();
            //Get player count for each server
            for (String server : servers) {
                CompletableFuture<Pair<String, Integer>> playerListFuture = new CompletableFuture<>();
                playerCountTasks.getOrDefault(server, new ArrayList<>()).add(playerListFuture);
                playerListsToWaitFor.add(playerListFuture);
                ByteArrayDataOutput playerCountMessage = ByteStreams.newDataOutput();
                playerCountMessage.writeUTF("PlayerCount");
                playerCountMessage.writeUTF(server);
                player.sendPluginMessage(null, HungerGamesLobby.BUNGEE_CHANNEL_NAME, playerCountMessage.toByteArray());
                playerListFuture.whenComplete((result, exception) -> {
                   playerCountMap.put(result.getKey(), result.getValue());
                });
            }
            CompletableFuture<Void> finalFuture = CompletableFuture.allOf(playerListsToWaitFor.toArray(new CompletableFuture[0]));
            finalFuture.whenComplete((nothing, exception) -> {
                playerCountTasks.clear();
               //Pick server from playerCountMap with fewest people, init arena on that server
            });
        });

    }

    public List<CompletableFuture<String>> getGetServersTasks() {
        return getServersTasks;
    }

    public HashMap<String, List<CompletableFuture<Pair<String, Integer>>>> getPlayerCountTasks() {
        return playerCountTasks;
    }
}
