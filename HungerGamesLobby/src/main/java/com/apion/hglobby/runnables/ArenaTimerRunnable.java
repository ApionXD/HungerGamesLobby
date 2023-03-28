package com.apion.hglobby.runnables;

import com.apion.hglobby.HungerGamesLobby;
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
    private final UUID queueUuid = UUID.randomUUID();
    private final NamespacedKey bossBarKey = new NamespacedKey(
            HungerGamesLobby.getInstance(),
            queueUuid.toString()
    );
    int delay;
    int currentTimerCountingDown;
    int maxPlayers;
    private BukkitTask countOffTask;
    String destination;
    String arenaName;

    /**
     * This class will periodically update the bossbar for the players in the playerList.
     * Upon first creation, it will also initialize a task to create the arena on the server
     * named destination that those players will be moved into when currentTimer = 0.
     * @param players Players to update with the bossbar
     * @param delay How long it will take to move players into their game
     * @param maxPlayers Max amount of players in the queue
     * @param destination Destination server to both create the arena and move the players into.
     */
    public ArenaTimerRunnable(final Queue<UUID> players, final int delay, final int maxPlayers, final String destination) {
        this.players = players;
        this.delay = delay;
        this.currentTimerCountingDown = delay;
        this.maxPlayers = maxPlayers;
        this.destination = destination;
        this.arenaName = UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public void run() {
        logger.info(MessageFormat.format("Queue {0} created with {1} player(s)", this.queueUuid, this.players.size()));
        // Create arena
        new CreateArenaRunnable(destination, arenaName).runTask(HungerGamesLobby.getInstance());
        // Update bossbar and cancel after delay, update every second
        countOffTask = new BukkitRunnable() {
            int runs = 0;
            @Override
            public void run() {
                currentTimerCountingDown -= 20;
                for (final UUID player : players) {
                    showArenaBossBarToPlayer(Bukkit.getPlayer(player));
                }
                runs++;
                if (runs >= delay / 20) {
                    final BossBar queueBossBar = Bukkit.getBossBar(bossBarKey);
                    Bukkit.getServer().removeBossBar(bossBarKey);

                    if (queueBossBar != null) {
                        queueBossBar.removeAll();
                        Bukkit.removeBossBar(bossBarKey);
                    }

                    new MovePlayersRunnable(players, bossBarKey, destination, arenaName).runTask(
                            HungerGamesLobby.getInstance()
                    );
                    this.cancel();
                }
            }
        }.runTaskTimer(HungerGamesLobby.getInstance(), 0, 20);
    }

    @Override
    public synchronized boolean isCancelled() throws IllegalStateException {
        if (countOffTask.isCancelled()) {
            this.cancel();
            return true;
        }
        return super.isCancelled();
    }

    public void forceAddPlayerToQueue(final UUID player) {
        players.add(player);
    }

    public boolean addPlayerToQueueIfPossible(final UUID player) {
        return notAtPlayerCap() && players.add(player);
    }

    public boolean notAtPlayerCap() {
        return players.size() <= HungerGamesLobby.getInstance().getIntFromConfirm("queue.maxPlayers");
    }

    public boolean isPlayerInQueue(final UUID playerUuid) {
        return players.contains(playerUuid);
    }

    /**
     * Finds the existing boss bar and adds the player to it.
     *
     * @param player Player to add
     * TODO: This doesn't work for old clients, need to check for old protocol version and
     * send them a chat message or something.
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
