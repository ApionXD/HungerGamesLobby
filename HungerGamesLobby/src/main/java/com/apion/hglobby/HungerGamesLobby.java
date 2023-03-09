package com.apion.hglobby;

import com.apion.hglobby.bungee.BungeeMessageListener;
import com.apion.hglobby.matchmake.QueueCommand;
import com.apion.hglobby.matchmake.QueueManager;
import com.apion.hglobby.server.HungeeServerExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class HungerGamesLobby extends JavaPlugin {
    private static HungerGamesLobby instance;
    public static QueueManager queueManager;
    public static BungeeMessageListener bungeeMessageListener;
    public static HungeeServerExecutor hungeeServerExecutor;

    @Override
    public void onEnable() {
        super.onEnable();
        this.saveDefaultConfig();

        instance = this;
        queueManager = new QueueManager();
        bungeeMessageListener = new BungeeMessageListener();
        hungeeServerExecutor = new HungeeServerExecutor();
        bungeeMessageListener.init();

        //noinspection DataFlowIssue
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

    public int getIntFromConfirm(final String configLocation) {
        return this.getConfig().getInt(configLocation);
    }

    public String getStringFromConfig(final String configLocation) {
        return this.getConfig().getString(configLocation);
    }
}
