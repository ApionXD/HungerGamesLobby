package com.apion.hgserver.lobby;

import com.apion.hgserver.HungerGamesServer;
import com.apion.hgserver.arena.ArenaInitializer;
import com.apion.hgserver.enums.ChannelNames;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@NoArgsConstructor
public class LobbyMessageHandler implements PluginMessageListener {
    private static Logger logger = Bukkit.getLogger();
    private ArenaInitializer arenaInitializer;


    public void init() {
        final HungerGamesServer instance = HungerGamesServer.getInstance();
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, ChannelNames.BUNGEE.channelName, this);
        arenaInitializer = new ArenaInitializer();
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (channel.equals(ChannelNames.BUNGEE.channelName)) {
            logger.warning("Got bungee message");
            ByteArrayDataInput messageWrapper = ByteStreams.newDataInput(message);
            String subchannel = messageWrapper.readUTF();
            if (subchannel.equals(ChannelNames.HUNGEE_GAMES_MANAGER.channelName)) {
                logger.warning("Got Hungee message");
                short msgLen = messageWrapper.readShort();
                byte[] messageBytes = new byte[msgLen];
                messageWrapper.readFully(messageBytes);
                String command = new String(messageBytes);
                switch (command) {
                    case "InitArena" -> {
                        logger.warning("Got message to init arena");
                        arenaInitializer.initializeArena();
                    }
                    default -> {
                        logger.severe("Got command from hub " + command + " but no actions defined");
                    }
                }
            }
        }
    }
}
