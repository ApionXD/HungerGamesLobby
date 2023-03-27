package com.apion.hgserver;

import com.apion.hgserver.arena.ArenaInitializer;
import com.apion.hgserver.database.Database;
import com.apion.hgserver.listener.PlayerJoinListener;
import com.apion.hgserver.lobby.LobbyMessageHandler;
import com.apion.hgserver.stats.StatTracker;
import com.onarandombox.MultiverseCore.MultiverseCore;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import tk.shanebee.hg.HG;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class HungerGamesServer extends JavaPlugin {
    private static Logger logger = Bukkit.getLogger();
    @Getter
    private static HungerGamesServer instance;
    @Getter
    private static HG hgPlugin;
    @Getter
    private static MultiverseCore mvPlugin;
    private static LobbyMessageHandler lobbyMessageHandler;
    private static ArenaInitializer arenaInitializer;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        super.onEnable();
        hgPlugin = (HG) Bukkit.getPluginManager().getPlugin("HungerGames");
        mvPlugin = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        Database.initConnection();
        arenaInitializer = new ArenaInitializer();
        lobbyMessageHandler = new LobbyMessageHandler(arenaInitializer);

        final List<Listener> listeners = new ArrayList<>();
        listeners.add( new PlayerJoinListener(arenaInitializer) );
        listeners.add( new StatTracker() );
        for (final Listener l : listeners) {
            Bukkit.getPluginManager().registerEvents(l, this);
        }
    }
}
