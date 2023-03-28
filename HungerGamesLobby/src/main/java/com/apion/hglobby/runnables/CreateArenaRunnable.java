package com.apion.hglobby.runnables;

import com.apion.hglobby.HungerGamesLobby;
import server.HungeeServerExecutor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class CreateArenaRunnable extends BukkitRunnable {
    final Logger logger = Bukkit.getLogger();
    String serverToCreateArena;
    String arenaName;

    @SuppressWarnings("unchecked")
    public CreateArenaRunnable(final String serverToCreateArena, final String arenaName) {
        this.serverToCreateArena = serverToCreateArena;
        this.arenaName = arenaName;
    }

    // 1. Get names of all servers
    // 2. Get player counts for each server
    // 3. Store player counts in playerCountMap
    // 4. Once we have all player counts, pick the optimal server
    @Override
    public void run() {
        logger.info("Sending arena initialization message to server: " + serverToCreateArena);
        HungeeServerExecutor serverMessage = HungerGamesLobby.getHungeeServerExecutor();
        serverMessage.sendInitArenaMessage(serverToCreateArena, arenaName, HungerGamesLobby.getInstance());
    }
}
