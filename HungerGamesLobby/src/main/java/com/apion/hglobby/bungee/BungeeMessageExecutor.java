package com.apion.hglobby.bungee;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hglobby.enums.BungeeMessageTypes;
import com.apion.hglobby.enums.ChannelNames;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BungeeMessageExecutor {
    private static final Logger logger = Bukkit.getLogger();

    /**
     * Puts a new future in the MessageHandler class to be completed,
     * then sends a plugin message to get all the servers on the bungeecord network
     *
     * @return Future of completed List<String> of servers
     */
    public static CompletableFuture<Object> getServerList() {
        final HungerGamesLobby instance = HungerGamesLobby.getInstance();
        final CompletableFuture<Object> future = new CompletableFuture<>();
        BungeeMessageHandler.putNewFuture(BungeeMessageTypes.GET_SERVERS.messageType, future);

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        ByteArrayDataOutput playerCountMessage = ByteStreams.newDataOutput();
        playerCountMessage.writeUTF(BungeeMessageTypes.GET_SERVERS.messageType);
        player.sendPluginMessage(instance, ChannelNames.BUNGEE.channelName, playerCountMessage.toByteArray());
        return future;
    }

    /**
     * @param server
     * @return
     */
    public static CompletableFuture<Object> getPlayerCountForServer(final String server) {
        final HungerGamesLobby instance = HungerGamesLobby.getInstance();
        final CompletableFuture<Object> future = new CompletableFuture<>();
        BungeeMessageHandler.putNewFuture(BungeeMessageTypes.PLAYER_COUNT.messageType, future);

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        ByteArrayDataOutput playerCountMessage = ByteStreams.newDataOutput();
        playerCountMessage.writeUTF(BungeeMessageTypes.PLAYER_COUNT.messageType);
        playerCountMessage.writeUTF(server);
        player.sendPluginMessage(instance, ChannelNames.BUNGEE.channelName, playerCountMessage.toByteArray());
        return future;
    }

    /**
     * Gets the server with the least amount of players on it.
     * @return CompletableFuture that will return the server with the lowest amount of players on it.
     */
    @SuppressWarnings("unchecked")
    public static CompletableFuture<String> getServerWithMinPlayers() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        getServerList().whenComplete(
                (servers, throwable) -> {
                    final Map<String, Integer> playerCountMap = new HashMap<>();

                    final List<String> serverList = new ArrayList<>((List<String>) servers);
                    logger.info("serverList size: " + serverList.size());
                    // Remove self from list
                    serverList.remove(HungerGamesLobby.getInstance().getStringFromConfig("bungee.serverName"));
                    // Get player count for each server
                    final CompletableFuture<Object>[] playerCountRequests = new CompletableFuture[serverList.size()];
                    final CompletableFuture<Void> playerCountDone = CompletableFuture.allOf(playerCountRequests);
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

                    playerCountDone.whenComplete(
                            (f, e) ->
                                    future.complete(
                                            playerCountMap.entrySet()
                                                    .stream()
                                                    .min(Map.Entry.comparingByValue())
                                                    .orElseThrow(() -> {
                                                        logger.severe("There were no servers to get the min players?");
                                                        return new IllegalStateException();
                                                    }).getKey()
                                    )
                    );
                }
        );
        return future;
    }
}
