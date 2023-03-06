package com.apion.hglobby;

import com.apion.hglobby.matchmake.QueueManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class HungerGamesLobby extends JavaPlugin implements PluginMessageListener {
    public static final String BUNGEE_CHANNEL_NAME = "BungeeCord";
    public static QueueManager queueManager;
    public static BungeeMessageHandler bungeeMessageHandler;
    @Override
    public void onEnable() {
        super.onEnable();
        queueManager = new QueueManager();
        bungeeMessageHandler = new BungeeMessageHandler(queueManager);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, BUNGEE_CHANNEL_NAME);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, BUNGEE_CHANNEL_NAME, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals(BUNGEE_CHANNEL_NAME)) {
            ByteArrayDataInput messageWrapper = ByteStreams.newDataInput(message);
            bungeeMessageHandler.handleMessage(messageWrapper);
        }
    }
}
