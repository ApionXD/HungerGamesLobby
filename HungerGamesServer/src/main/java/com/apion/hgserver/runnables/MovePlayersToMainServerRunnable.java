package com.apion.hgserver.runnables;

import com.apion.hgserver.HungerGamesServer;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@AllArgsConstructor
public class MovePlayersToMainServerRunnable extends BukkitRunnable {
    private final Logger logger = Bukkit.getLogger();
    private final List<UUID> players;
    private final String serverName;
    @Override
    public void run() {
        logger.info(MessageFormat.format("Moving {0} players into {1}", players.size(), serverName));
        for (final UUID playerUuid : players) {
            final Player player = Optional.ofNullable(Bukkit.getPlayer(playerUuid))
                    .orElseThrow(() -> {
                        logger.warning("Player " + playerUuid + " wasn't present to move");
                        return new IllegalStateException();
                    });

            logger.info(MessageFormat.format("Moving {0} to server {1}", player.getName(), serverName));
            HungerGamesServer.getHungeeServerExecutor()
                    .sendMovePlayerMessage(
                            serverName,
                            player,
                            HungerGamesServer.getInstance()
                    );
        }
    }
}
