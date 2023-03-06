package com.apion.hglobby;

import com.apion.hglobby.bungee.BungeeMessageHandler;
import com.apion.hglobby.bungee.BungeeMessageListener;
import com.apion.hglobby.matchmake.QueueCommand;
import com.apion.hglobby.matchmake.QueueManager;
import com.apion.hglobby.server.HungeeServerExecutor;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HungerGamesLobby extends JavaPlugin {
    private static HungerGamesLobby instance;
    public static QueueManager queueManager;
    public static BungeeMessageListener bungeeMessageListener;
    public static HungeeServerExecutor hungeeServerExecutor;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        queueManager = new QueueManager();
        bungeeMessageListener = new BungeeMessageListener();
        hungeeServerExecutor = new HungeeServerExecutor();
        bungeeMessageListener.init();
        this.getCommand("queue").setExecutor(new QueueCommand());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        bungeeMessageListener.deInit();
    }

    public static HungerGamesLobby getInstance() {
        return instance;
    }
}
