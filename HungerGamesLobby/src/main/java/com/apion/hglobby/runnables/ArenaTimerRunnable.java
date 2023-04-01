package com.apion.hglobby.runnables;

import com.apion.hglobby.HungerGamesLobby;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

public class ArenaTimerRunnable extends BukkitRunnable {
    private final Logger logger = Bukkit.getLogger();
    private final Queue<UUID> players;
    private final String BOSS_BAR_TITLE =
            ChatColor.BLUE + ChatColor.BOLD.toString() + "%s / %s possible players" +
                    ChatColor.RESET + ChatColor.LIGHT_PURPLE + " %ss left until start!";
    @Getter
    private final UUID queueUuid;
    private final NamespacedKey bossBarKey;
    int delay;
    int currentTimerCountingDown;
    int maxPlayers;
    private BukkitTask countOffTask;
    String destination;
    String arenaName;
    int playersNeededToStartArena;
    boolean taskKilled;

    /**
     * This class will periodically update the bossbar for the players in the playerList.
     * Upon first creation, it will also initialize a task to create the arena on the server
     * named destination that those players will be moved into when currentTimer = 0.
     *
     * @param players     Players to update with the bossbar
     * @param delay       How long it will take to move players into their game
     * @param maxPlayers  Max amount of players in the queue
     * @param destination Destination server to both create the arena and move the players into.
     */
    public ArenaTimerRunnable(
            final Queue<UUID> players,
            final int delay,
            final int requiredPlayers,
            final int maxPlayers,
            final String destination,
            final UUID queueUuid
    ) {
        this.players = players;
        this.delay = delay;
        this.currentTimerCountingDown = delay;
        this.playersNeededToStartArena = requiredPlayers;
        this.maxPlayers = maxPlayers;
        this.destination = destination;
        this.arenaName = UUID.randomUUID().toString().substring(0, 8);
        this.queueUuid = queueUuid;
        bossBarKey = new NamespacedKey(
                HungerGamesLobby.getInstance(),
                queueUuid.toString()
        );
    }

    @Override
    public void run() {
        logger.info(MessageFormat.format("Queue {0} created with {1} player(s)", this.queueUuid, this.players.size()));
        // Create arena
        new CreateArenaRunnable(destination, arenaName).runTask(HungerGamesLobby.getInstance());
        // Update bossbar and cancel after delay, update every second
        countOffTask = new BukkitRunnable() {
            final UUID parentQueueUuid = queueUuid;
            int runs = 0;

            @Override
            public void run() {
                final Optional<ArenaTimerRunnable> parent = HungerGamesLobby.queueManager.getQueueInProgress(parentQueueUuid);
                final boolean parentIsCancelled = parent.isEmpty() || parent.get().taskKilled;
                logger.info("parent uuid: " + parentQueueUuid);
                logger.info("parent: " + parent.isEmpty());
                logger.info("parent cancelled: " + parentIsCancelled);

                currentTimerCountingDown -= 20;
                for (final UUID player : players) {
                    showArenaBossBarToPlayer(Bukkit.getPlayer(player));
                }
                runs++;
                if (runs >= delay / 20 || parentIsCancelled) {
                    final BossBar queueBossBar = Bukkit.getBossBar(bossBarKey);
                    Bukkit.getServer().removeBossBar(bossBarKey);

                    if (queueBossBar != null) {
                        queueBossBar.removeAll();
                        Bukkit.removeBossBar(bossBarKey);
                    }

                    if (!parentIsCancelled) {
                        new MovePlayersToArenaRunnable(players, bossBarKey, destination, arenaName).runTask(
                                HungerGamesLobby.getInstance()
                        );
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(HungerGamesLobby.getInstance(), 0, 20);
    }

    @Override
    public synchronized boolean isCancelled() throws IllegalStateException {
        if (countOffTask.isCancelled() || taskKilled) {
            this.cancel();
            return true;
        }
        return super.isCancelled();
    }

    public void forceAddPlayerToQueueAboutToStart(final UUID player) {
        players.add(player);
    }

    public boolean addPlayerToQueueAboutToStartIfPossible(final UUID player) {
        synchronized (players) {
            final int TWO_SECONDS_IN_TICKS = 40;
            return notAtPlayerCap() &&
                    currentTimerCountingDown >= TWO_SECONDS_IN_TICKS &&
                    players.add(player);
        }
    }

    public boolean notAtPlayerCap() {
        return players.size() <= HungerGamesLobby.getInstance().getIntFromConfirm("queue.maxPlayers");
    }

    public boolean isPlayerInQueueAboutToStart(final UUID playerUuid) {
        return players.contains(playerUuid);
    }

    public void removePlayerFromQueueAboutToStartIfPresent(final UUID removedPlayerUuid) {
        synchronized (players) {
            players.remove(removedPlayerUuid);

            if (players.size() <= playersNeededToStartArena) {
                final List<UUID> copyOfPlayers = new ArrayList<>(players);
                final boolean canMerge = HungerGamesLobby.queueManager.canMergeQueues(copyOfPlayers);

                if (!canMerge) {
                    copyOfPlayers.forEach(playerUuid -> {
                        final Player player = Bukkit.getPlayer(playerUuid);
                        if (player != null) {
                            player.sendMessage("Someone left your queue and it wasn't possible to merge with the existing queue. Please queue again!");
                        }
                    });
                    this.cancel();
                    this.taskKilled = true;
                    return;
                }

                players.clear();
                this.cancel();
                this.taskKilled = true;
                copyOfPlayers.forEach( playerUuid -> {
                    final Player player = Bukkit.getPlayer(playerUuid);
                    if (player != null) {
                        player.sendMessage("Someone left your queue in progress, sending you back to the main queue.");
                        HungerGamesLobby.queueManager.registerIntoQueue(player);
                    }
                });
            }

            final BossBar bossBar = Bukkit.getBossBar(bossBarKey);

            if (bossBar == null) {
                return;
            }

            final Player player = Bukkit.getPlayer(removedPlayerUuid);
            if (player == null) {
                return;
            }

            bossBar.removePlayer(player);
        }
    }

    /**
     * Finds the existing boss bar and adds the player to it.
     *
     * @param player Player to add
     */
    private void showArenaBossBarToPlayer(final Player player) {
        String bossBarTitle = String.format(BOSS_BAR_TITLE, players.size(), maxPlayers, currentTimerCountingDown / 20);
        KeyedBossBar queueBossBar = Bukkit.getBossBar(bossBarKey);

        if (queueBossBar == null) {
            queueBossBar = Bukkit.createBossBar(
                    bossBarKey,
                    bossBarTitle,
                    BarColor.RED,
                    BarStyle.SEGMENTED_12
            );
        }
        queueBossBar.setTitle(bossBarTitle);
        queueBossBar.addPlayer(player);
        queueBossBar.setVisible(true);
        queueBossBar.setProgress(players.size() / (double) maxPlayers);
    }
}
