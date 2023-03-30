package com.apion.hglobby.runnables;

import com.apion.hglobby.HungerGamesLobby;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Logger;

public class MovePlayersToArenaRunnable extends BukkitRunnable {
    private static final Logger logger = Bukkit.getLogger();
    private final Queue<UUID> players;
    private final NamespacedKey bossBarKey;
    private final String serverName;
    private final String arenaName;

    public MovePlayersToArenaRunnable(Queue<UUID> players, NamespacedKey bossBarKey, String serverName, String arenaName) {
        this.players = players;
        this.bossBarKey = bossBarKey;
        this.serverName = serverName;
        this.arenaName = arenaName;
    }

    @Override
    public void run() {
        final BossBar queueBossBar = Bukkit.getBossBar(bossBarKey);

        if (queueBossBar != null) {
            queueBossBar.removeAll();
            Bukkit.removeBossBar(bossBarKey);
        }
        logger.info(MessageFormat.format("Moving {0} players into {1}", players.size(), serverName));
        HungerGamesLobby.hungeeServerExecutor.sendArenaMoveMessage(
                serverName,
                arenaName,
                players,
                HungerGamesLobby.getInstance()
        );
        for (final UUID playerUuid : players) {
            final Player player = Optional.ofNullable(Bukkit.getPlayer(playerUuid))
                    .orElseThrow(() -> {
                        logger.warning("Player " + playerUuid + " wasn't present to move");
                        return new IllegalStateException();
                    });

            logger.info(MessageFormat.format("Moving {0} to server {1}", player.getName(), serverName));
            HungerGamesLobby.hungeeServerExecutor
                    .sendMovePlayerMessage(
                            serverName,
                            player,
                            HungerGamesLobby.getInstance()
                    );
        }
    }
}
