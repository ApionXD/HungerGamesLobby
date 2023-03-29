package com.apion.hgserver.listener;

import com.apion.hgserver.HungerGamesServer;
import com.apion.hgserver.runnables.MovePlayersToMainServerRunnable;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tk.shanebee.hg.events.GameEndEvent;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GameEndListener implements Listener {
    private static final Logger logger = Bukkit.getLogger();
    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        MultiverseCore mvPlugin = HungerGamesServer.getMvPlugin();
        String worldName = event.getGame().getGameArenaData().getBound().getWorld().getName();
        logger.info(String.format("Arena %s is ending", worldName));
        List<UUID> players = Bukkit.getServer().getWorld(worldName).getPlayers().stream().map(Entity::getUniqueId).collect(Collectors.toList());
        new MovePlayersToMainServerRunnable(players).runTask(HungerGamesServer.getInstance());
        mvPlugin.getMVWorldManager().deleteWorld(worldName, true, true);
        HungerGamesServer.getHgPlugin().getGames().removeIf(g -> g.getGameArenaData().getName().equals(worldName));
    }
}
