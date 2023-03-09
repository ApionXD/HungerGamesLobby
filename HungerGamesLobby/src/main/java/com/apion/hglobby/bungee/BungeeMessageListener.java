package com.apion.hglobby.bungee;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hglobby.enums.ChannelNames;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class BungeeMessageListener implements PluginMessageListener {
    private final BungeeMessageHandler handler = new BungeeMessageHandler();

    public void init() {
        final HungerGamesLobby instance = HungerGamesLobby.getInstance();
        instance.getServer().getMessenger().registerOutgoingPluginChannel(instance, ChannelNames.BUNGEE.channelName);
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, ChannelNames.BUNGEE.channelName, this);
    }

    public void deInit() {
        final HungerGamesLobby instance = HungerGamesLobby.getInstance();
        instance.getServer().getMessenger().unregisterOutgoingPluginChannel(instance);
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        handler.handleMessage(in);
    }
}
