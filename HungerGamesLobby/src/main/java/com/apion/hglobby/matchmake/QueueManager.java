package com.apion.hglobby.matchmake;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hglobby.bungee.BungeeMessageExecutor;
import com.apion.hglobby.server.HungeeServerExecutor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class QueueManager {

    private static final Logger logger = Bukkit.getLogger();
    //List of players in queue
    private Queue<UUID> playerList;
    private Map<String, Integer> playerCountMap;
    final int requiredPlayersToQueue = 12;
    final String BOSS_BAR_TITLE =
            ChatColor.BLUE + ChatColor.BOLD.toString() + "%s / %s needed players" +
                    ChatColor.RESET + ChatColor.LIGHT_PURPLE + " Currently In Queue";
    final NamespacedKey queueNamespacedKey = new NamespacedKey(
            HungerGamesLobby.getInstance(),
            "queuebar"
    );

    public QueueManager() {
        playerList = new LinkedList<>();
    }

    public synchronized void registerIntoQueue(final Player player) {
        playerList.add(player.getUniqueId());

        showQueueBossBarToPlayer(player);

        if (playerList.size() > requiredPlayersToQueue) {
            Bukkit.removeBossBar(queueNamespacedKey);
            // maybe should start task to wait 30s before starting arena, to allow for 12-16 players?
            // either we hit 16 and remove all 16 from the queue and immediately create the arena
            // or just wait the 30s and create the arena when it's done
            // createArena();
        }
    }

    //This method is a lil complex
    // 1. Get names of all servers
    // 2. Get player counts for each server
    // 3. Store player counts in playerCountMap
    // 4. Once we have all player counts, pick the optimal server
    @SuppressWarnings("unchecked")
    public void createArena() {

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

    /**
     * Finds the existing boss bar and adds the player to it.
     *
     * @param player Player to add
     *               TODO: This doesn't work for old clients, need to check for old protocol version and
     *               send them a chat message or something.
     */
    private void showQueueBossBarToPlayer(final Player player) {
        String bossBarTitle = String.format(BOSS_BAR_TITLE, playerList.size(), requiredPlayersToQueue);
        KeyedBossBar queueBossBar = Bukkit.getBossBar(queueNamespacedKey);

        if (queueBossBar == null) {
            queueBossBar = Bukkit.createBossBar(
                    queueNamespacedKey,
                    bossBarTitle,
                    BarColor.RED,
                    BarStyle.SEGMENTED_12
            );
        }
        queueBossBar.setTitle(bossBarTitle);
        queueBossBar.addPlayer(player);
        queueBossBar.setVisible(true);
        queueBossBar.setProgress(playerList.size() / 12.0);
    }
}
