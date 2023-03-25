package com.apion.hgserver;

import com.apion.hgserver.lobby.LobbyMessageHandler;
import com.onarandombox.MultiverseCore.MultiverseCore;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import tk.shanebee.hg.HG;

public class HungerGamesServer extends JavaPlugin {
    @Getter
    private static HungerGamesServer instance;
    @Getter
    private static HG hgPlugin;
    @Getter
    private static MultiverseCore mvPlugin;

    private static LobbyMessageHandler lobbyMessageHandler;

    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();
        hgPlugin = (HG) Bukkit.getPluginManager().getPlugin("HungerGames");
        mvPlugin = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        lobbyMessageHandler = new LobbyMessageHandler();
        lobbyMessageHandler.init();
    }
}
