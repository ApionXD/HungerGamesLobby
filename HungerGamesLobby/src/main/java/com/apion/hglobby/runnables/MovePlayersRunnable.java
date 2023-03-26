package com.apion.hglobby.runnables;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hglobby.enums.ChannelNames;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.MessageFormat;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Logger;

public class MovePlayersRunnable extends BukkitRunnable {
    private static final Logger logger = Bukkit.getLogger();
    private final Queue<UUID> players;
    private final NamespacedKey bossBarKey;
    private final String serverName;

    public MovePlayersRunnable(Queue<UUID> players, NamespacedKey bossBarKey, String serverName) {
        this.players = players;
        this.bossBarKey = bossBarKey;
        this.serverName = serverName;
    }

    @Override
    public void run() {
        final BossBar queueBossBar = Bukkit.getBossBar(bossBarKey);

        if (queueBossBar != null) {
            queueBossBar.removeAll();
            Bukkit.removeBossBar(bossBarKey);
        }
        logger.warning(MessageFormat.format("Moving {0} players into {1}", players.size(), serverName));
        for (UUID p : players) {
            ByteArrayDataOutput message = ByteStreams.newDataOutput();
            message.writeUTF("Connect");
            message.writeUTF(serverName);
            Bukkit.getPlayer(p).sendPluginMessage(HungerGamesLobby.getInstance(), ChannelNames.BUNGEE.channelName, message.toByteArray());
        }
    }
}
