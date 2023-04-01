package com.apion.hgserver.listener;

import com.apion.hgserver.HungerGamesServer;
import com.apion.hgserver.arena.ArenaInitializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {
    private final ArenaInitializer arenaInitializer;
    public PlayerJoinListener(ArenaInitializer initializer) {
        arenaInitializer = initializer;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                arenaInitializer.putPlayerInArena(event.getPlayer().getUniqueId());
            }
        }.runTaskLater(HungerGamesServer.getInstance(), 5);

    }
}
