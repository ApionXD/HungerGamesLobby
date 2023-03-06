package com.apion.hgserver.lobby;

import com.apion.hgserver.HungerGamesServer;
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
    public static final String LOBBY_CHANNEL_NAME = "bungeegames:main";

    public void init() {
        final HungerGamesServer instance = HungerGamesServer.getInstance();
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, LOBBY_CHANNEL_NAME, this);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        logger.warning(channel);
        if (channel.equals(LOBBY_CHANNEL_NAME)) {
            ByteArrayDataInput messageWrapper = ByteStreams.newDataInput(message);
            String subchannel = messageWrapper.readUTF();
            switch (subchannel) {
                case "ArenaInit" -> {
                    System.out.println("Got message to init arena");
                }
            }
        }
    }
}
