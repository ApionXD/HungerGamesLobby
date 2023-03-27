package com.apion.hgserver.lobby;

import com.apion.hgserver.HungerGamesServer;
import com.apion.hgserver.arena.ArenaInitializer;
import com.apion.hgserver.enums.ChannelNames;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

public class LobbyMessageHandler implements PluginMessageListener {
    private static Logger logger = Bukkit.getLogger();
    private ArenaInitializer arenaInitializer;
    public LobbyMessageHandler(ArenaInitializer initializer) {
        final HungerGamesServer instance = HungerGamesServer.getInstance();
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, ChannelNames.BUNGEE.channelName, this);
        arenaInitializer = initializer;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (channel.equals(ChannelNames.BUNGEE.channelName)) {
            logger.info("Got bungee message");
            ByteArrayDataInput messageWrapper = ByteStreams.newDataInput(message);
            String subchannel = messageWrapper.readUTF();
            if (subchannel.equals(ChannelNames.HUNGEE_GAMES_MANAGER.channelName)) {
                logger.info("Got Hungee message");
                short msgLen = messageWrapper.readShort();
                byte[] messageBytes = new byte[msgLen];
                messageWrapper.readFully(messageBytes);
                String messageStr = new String(messageBytes);
                String[] messageContents = messageStr.split(" ");
                switch (messageContents[0]) {
                    case "InitArena" -> {
                        String arenaName = messageContents[1];
                        logger.info("Got message to init arena " + arenaName);
                        arenaInitializer.initializeArena(arenaName);
                    }
                    case "ArenaMove" -> {
                        String arena = messageContents[1];
                        String uuids = messageContents[2];
                        logger.info("Moving players to " + arena);
                        Arrays.stream(uuids.split(",")).map(UUID::fromString).forEach(uuid -> arenaInitializer.addPlayerToMap(uuid, arena));
                    }
                    default -> logger.severe("Got command from hub " + messageContents[0] + " but no actions defined");
                }
            }
        }
    }
}
