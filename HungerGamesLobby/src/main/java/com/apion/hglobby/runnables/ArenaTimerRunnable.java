package com.apion.hglobby.runnables;

import com.apion.hglobby.HungerGamesLobby;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.MessageFormat;
import java.util.Queue;
import java.util.UUID;
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
    int currentTimer;
    int maxPlayers;

    public ArenaTimerRunnable (final Queue<UUID> players, final int delay, final int maxPlayers) {
        for (UUID uuid : players) {
            System.out.println(uuid);
        }
        this.players = players;
        this.delay = delay;
        this.currentTimer = delay;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public void run() {
        logger.info(MessageFormat.format("Queue {0} created with {1} player(s)", this.queueUuid, this.players.size()));
        // Update bossbar and cancel after delay, update every second
        new BukkitRunnable() {
            int runs = 0;
            @Override
            public void run() {
                currentTimer -= 20;
                for (final UUID player : players) {
                    showArenaBossBarToPlayer(Bukkit.getPlayer(player));
                }
                runs++;
                if (runs >= delay / 20) {
                    this.cancel();
                }
            }
        }.runTaskTimer(HungerGamesLobby.getInstance(), 0, 20);

        // Create arena
        new CreateArenaRunnable(players, bossBarKey, delay).runTask(HungerGamesLobby.getInstance());
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
        String bossBarTitle = String.format(BOSS_BAR_TITLE, players.size(), maxPlayers, currentTimer / 20);
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
