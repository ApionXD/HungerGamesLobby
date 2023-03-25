package com.apion.hgserver;

import com.apion.hgserver.database.Database;
import com.apion.hgserver.lobby.LobbyMessageHandler;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.onarandombox.MultiverseCore.MultiverseCore;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import tk.shanebee.hg.HG;

import java.sql.Connection;
import java.sql.SQLException;
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

    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();
        hgPlugin = (HG) Bukkit.getPluginManager().getPlugin("HungerGames");
        mvPlugin = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        Database.initConnection();
        lobbyMessageHandler = new LobbyMessageHandler();
        lobbyMessageHandler.init();
    }
}
