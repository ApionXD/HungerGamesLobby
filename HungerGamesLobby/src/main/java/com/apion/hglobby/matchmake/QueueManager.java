package com.apion.hglobby.matchmake;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hglobby.runnables.ArenaTimerRunnable;

import java.text.MessageFormat;
import java.util.*;

import com.apion.hglobby.runnables.Futures;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;


public class QueueManager {

    private static final Logger logger = Bukkit.getLogger();
    //List of players in queue
    private Queue<UUID> playerList;
    final String BOSS_BAR_TITLE =
            ChatColor.BLUE + ChatColor.BOLD.toString() + "%s / %s needed players" +
                    ChatColor.RESET + ChatColor.LIGHT_PURPLE + " Currently In Queue";
    final NamespacedKey queueNamespacedKey = new NamespacedKey(
            HungerGamesLobby.getInstance(),
            "queuebar"
    );
    final BossBar queueBossBar;
    final List<ArenaTimerRunnable> queuesInProgress;

    public QueueManager() {
        playerList = new LinkedList<>();
        queuesInProgress = new ArrayList<>();
        queueBossBar = Bukkit.createBossBar(
                queueNamespacedKey,
                BOSS_BAR_TITLE,
                BarColor.RED,
                BarStyle.SEGMENTED_12
        );
    }

    public boolean isPlayerInQueue(final Player player) {
        return isPlayerInQueue(player.getUniqueId());
    }

    public boolean isPlayerInQueue(final UUID playerUuid) {
        return queuesInProgress.stream().anyMatch(queue -> queue.isPlayerInQueue(playerUuid));
    }

    public void removeFromQueueIfPresent(final Player player) {
        removeFromQueueIfPresent(player.getUniqueId());
    }

    public void removeFromQueueIfPresent(final UUID playerUuid) {
        if (queuesInProgress.isEmpty()) {
            return;
        }

        queuesInProgress.forEach( queue -> {
            if (queue.isPlayerInQueue(playerUuid)) {
                queue.removePlayerFromQueueIfPresent(playerUuid);
            }
        });
    }

    public void registerIntoQueue(final Player player) {
        //Removes all finished queues
        queuesInProgress.removeIf(BukkitRunnable::isCancelled);
        final UUID playerUUID = player.getUniqueId();
        boolean addedPlayerToExistingQueue = false;
        // Check if any queues are in progress and have space
        if (!queuesInProgress.isEmpty()) {
            final ArenaTimerRunnable queue = queuesInProgress
                    .stream().filter(ArenaTimerRunnable::notAtPlayerCap)
                    .findFirst()
                    .orElse(null);

            if (queue != null) {
                addedPlayerToExistingQueue = queue.addPlayerToQueueIfPossible(playerUUID);
                if (!addedPlayerToExistingQueue) {
                    logger.severe(MessageFormat.format("Couldn't add {0} to queue {1}", playerUUID, queuesInProgress.indexOf(queue)));
                }
            }
        }

        if (!addedPlayerToExistingQueue) {
            playerList.add(player.getUniqueId());
            final int requiredPlayers = HungerGamesLobby.getInstance().getIntFromConfirm("queue.requiredPlayers");

            if (playerList.size() >= requiredPlayers) {
                logger.info("Getting lowest player count server and sending to Arena queue runnable");
                new Futures().getServerWithMinPlayers()
                        .whenComplete(
                                (creatingOnServer, throwable) -> {
                                    queueBossBar.removeAll();
                                    final ArenaTimerRunnable runnable = new ArenaTimerRunnable(
                                            new LinkedList<>(playerList),
                                            HungerGamesLobby.getInstance().getIntFromConfirm("queue.delayToRunArena"),
                                            HungerGamesLobby.getInstance().getIntFromConfirm("queue.maxPlayers"),
                                            creatingOnServer
                                    );
                                    runnable.runTask(HungerGamesLobby.getInstance());
                                    queuesInProgress.add(runnable);
                                    playerList.clear();
                                }
                        );
            }
            else {
                showQueueBossBarToPlayer(player, requiredPlayers);
            }
        }
    }

    /**
     * Finds the existing boss bar and adds the player to it.
     *
     * @param player Player to add
     * TODO: This doesn't work for old clients, need to check for old protocol version and
     * send them a chat message or something.
     */
    private void showQueueBossBarToPlayer(final Player player, final int requiredPlayers) {
        String bossBarTitle = String.format(BOSS_BAR_TITLE, playerList.size(), requiredPlayers);

        queueBossBar.setTitle(bossBarTitle);
        queueBossBar.addPlayer(player);
        queueBossBar.setVisible(true);
        queueBossBar.setProgress(playerList.size() / 12.0);
    }
}
