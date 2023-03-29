package com.apion.hglobby.listeners;

import com.apion.hglobby.HungerGamesLobby;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        HungerGamesLobby.queueManager.removeFromQueueIfPresent(event.getPlayer());
    }
}
