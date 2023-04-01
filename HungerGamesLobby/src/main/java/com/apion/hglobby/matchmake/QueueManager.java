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
import java.util.stream.Collectors;


public class QueueManager {

    private static final Logger logger = Bukkit.getLogger();
    //List of players in queue
    final private Queue<UUID> playerList;
    final String BOSS_BAR_TITLE =
            ChatColor.BLUE + ChatColor.BOLD.toString() + "%s / %s needed players" +
            ChatColor.RESET + ChatColor.LIGHT_PURPLE + " Currently In Queue";
    final NamespacedKey queueNamespacedKey = new NamespacedKey(
            HungerGamesLobby.getInstance(),
            "queuebar"
    );
    final BossBar queueBossBar;
    final private HashMap<UUID, ArenaTimerRunnable> queuesInProgress;
    final private int requiredPlayersToStartArenaRunnable;
    final private int maxPlayers;

    public QueueManager() {
        playerList = new LinkedList<>();
        queuesInProgress = new HashMap<>();
        queueBossBar = Bukkit.createBossBar(
                queueNamespacedKey,
                BOSS_BAR_TITLE,
                BarColor.RED,
                BarStyle.SEGMENTED_12
        );
        requiredPlayersToStartArenaRunnable = HungerGamesLobby.getInstance().getIntFromConfirm("queue.requiredPlayers");
        maxPlayers = HungerGamesLobby.getInstance().getIntFromConfirm("queue.maxPlayers");
    }

    public boolean isPlayerInQueue(final Player player) {
        return isPlayerInQueue(player.getUniqueId());
    }

    public boolean isPlayerInQueue(final UUID playerUuid) {
        final boolean queueHasPlayer = playerList.contains(playerUuid);
        final boolean queueAboutToStartHasPlayer = queuesInProgress.values()
                .stream()
                .anyMatch(queue -> queue.isPlayerInQueueAboutToStart(playerUuid));

        return queueHasPlayer || queueAboutToStartHasPlayer;
    }

    public void removeFromQueueIfPresent(final Player player) {
        removeFromQueueIfPresent(player.getUniqueId());
    }

    public void removeFromQueueIfPresent(final UUID playerUuid) {
        if (playerList.isEmpty() && queuesInProgress.isEmpty()) {
            return;
        }

        final boolean elementRemoved = playerList.remove(playerUuid);
        if (!elementRemoved) {
            final Optional<Map.Entry<UUID, ArenaTimerRunnable>> inQueue = queuesInProgress.entrySet()
                    .stream()
                    .filter(queue -> queue.getValue().isPlayerInQueueAboutToStart(playerUuid))
                    .findFirst();

            inQueue
                    .ifPresent(uuidArenaTimerRunnableEntry -> queuesInProgress.get(uuidArenaTimerRunnableEntry.getKey())
                            .removePlayerFromQueueAboutToStartIfPresent(playerUuid));
        }
        else {
            final Player player = Bukkit.getPlayer(playerUuid);
            if (player != null) {
                queueBossBar.removePlayer(player);
            }
        }
    }

    public void registerIntoQueue(final Player player) {
        boolean addedPlayerToExistingQueue = false;

        synchronized (queuesInProgress) {
            //Removes all finished queues
            queuesInProgress.values().stream()
                    .filter(BukkitRunnable::isCancelled)
                    .map(ArenaTimerRunnable::getQueueUuid)
                    .toList()
                    .forEach(queuesInProgress::remove);

            final UUID playerUUID = player.getUniqueId();
            // Check if any queues are in progress and have space
            if (!queuesInProgress.isEmpty()) {
                final ArenaTimerRunnable queue = queuesInProgress
                        .values()
                        .stream()
                        .filter(ArenaTimerRunnable::notAtPlayerCap)
                        .findFirst()
                        .orElse(null);

                if (queue != null) {
                    addedPlayerToExistingQueue = queue.addPlayerToQueueAboutToStartIfPossible(playerUUID);
                    if (!addedPlayerToExistingQueue) {
                        logger.severe(MessageFormat.format("Couldn't add {0} to queue {1}", playerUUID, queuesInProgress.get(queue)));
                    }
                }
            }
        }

        synchronized (playerList) {
            if (!addedPlayerToExistingQueue) {
                playerList.add(player.getUniqueId());

                if (playerList.size() >= requiredPlayersToStartArenaRunnable) {
                    logger.info("Getting lowest player count server and sending to Arena queue runnable");
                    new Futures().getServerWithMinPlayers()
                            .whenComplete(
                                    (creatingOnServer, throwable) -> {
                                        queueBossBar.removeAll();
                                        final UUID subQueueId = UUID.randomUUID();
                                        final ArenaTimerRunnable runnable = new ArenaTimerRunnable(
                                                new LinkedList<>(playerList),
                                                HungerGamesLobby.getInstance().getIntFromConfirm("queue.delayToRunArena"),
                                                requiredPlayersToStartArenaRunnable,
                                                maxPlayers,
                                                creatingOnServer,
                                                subQueueId
                                        );
                                        runnable.runTask(HungerGamesLobby.getInstance());
                                        queuesInProgress.put(subQueueId, runnable);
                                        playerList.clear();
                                    }
                            );
                }
                else {
                    showQueueBossBarToPlayer(player, requiredPlayersToStartArenaRunnable);
                }
            }
        }
    }

    public boolean canMergeQueues(final List<UUID> queueToMergeIn) {
        synchronized (playerList) {
            final int queueToMergeInSizePlusCurrentPlayers = queueToMergeIn.size() + playerList.size();
            return queueToMergeInSizePlusCurrentPlayers <= maxPlayers;
        }
    }

    public void mergeQueueIntoCurrentQueue(final List<UUID> queueToMergeIn) {
        synchronized (playerList) {
            for (final UUID playerUuid : queueToMergeIn) {
                final Player player = Bukkit.getPlayer(playerUuid);
                if (player != null) {
                    registerIntoQueue(player);
                }
            }
        }
    }

    public Optional<ArenaTimerRunnable> getQueueInProgress(final UUID queueUuid) {
        return Optional.ofNullable(queuesInProgress.get(queueUuid));
    }

    public void removeQueueInProgress(final UUID queueUuid) {
        queuesInProgress.remove(queueUuid);
    }

    /**
     * Finds the existing boss bar and adds the player to it.
     *
     * @param player Player to add
     *               TODO: This doesn't work for old clients, need to check for old protocol version and
     *               send them a chat message or something.
     */
    private void showQueueBossBarToPlayer(final Player player, final int requiredPlayers) {
        String bossBarTitle = String.format(BOSS_BAR_TITLE, playerList.size(), requiredPlayers);

        queueBossBar.setTitle(bossBarTitle);
        queueBossBar.addPlayer(player);
        queueBossBar.setVisible(true);
        queueBossBar.setProgress(playerList.size() / 12.0);
    }
}
