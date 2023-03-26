package com.apion.hgserver.listener;

import com.apion.hgserver.arena.ArenaInitializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final ArenaInitializer arenaInitializer;
    public PlayerJoinListener(ArenaInitializer initializer) {
        arenaInitializer = initializer;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        arenaInitializer.putPlayerInArena(event.getPlayer().getUniqueId());
    }
}
