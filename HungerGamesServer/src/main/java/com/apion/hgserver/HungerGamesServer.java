package com.apion.hgserver;

import com.apion.hgserver.lobby.LobbyMessageHandler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class HungerGamesServer extends JavaPlugin {
    @Getter
    private static HungerGamesServer instance;

    private static LobbyMessageHandler lobbyMessageHandler;

    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();
        lobbyMessageHandler = new LobbyMessageHandler();
        lobbyMessageHandler.init();
    }
}
