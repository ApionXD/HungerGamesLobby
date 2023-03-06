package com.apion.hglobby.bungee;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hglobby.enums.BungeeMessageTypes;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BungeeMessageExecutor {

  /**
   * Puts a new future in the MessageHandler class to be completed,
   * then sends a plugin message to get all the servers on the bungeecord network
   * @return Future of completed List<String> of servers
   */
  public static CompletableFuture<Object> getServerList() {
    final HungerGamesLobby instance = HungerGamesLobby.getInstance();
    final CompletableFuture<Object> future = new CompletableFuture<>();
    BungeeMessageHandler.putNewFuture(BungeeMessageTypes.GET_SERVERS.messageType, future);

    Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
    ByteArrayDataOutput playerCountMessage = ByteStreams.newDataOutput();
    playerCountMessage.writeUTF(BungeeMessageTypes.GET_SERVERS.messageType);
    player.sendPluginMessage(instance, BungeeMessageListener.BUNGEE_CHANNEL_NAME, playerCountMessage.toByteArray());
    return future;
  }

  /**
   *
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
    player.sendPluginMessage(instance, BungeeMessageListener.BUNGEE_CHANNEL_NAME, playerCountMessage.toByteArray());
    return future;
  }
}
