package com.apion.hgserver.arena;

import com.apion.hgserver.HungerGamesServer;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.game.Game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class ArenaInitializer {
    private static final Logger logger = Bukkit.getLogger();
    private final HG hgPlugin = HungerGamesServer.getHgPlugin();
    private final MultiverseCore mvPlugin = HungerGamesServer.getMvPlugin();
    public ArenaInitializer() {

    }

    //Select random world
    //Create new Multiverse world from template world using multiverse clone
    //Create arena from world
    public void initializeArena() {
        String templateArenaName = "template1";
        List<Game> arenas = hgPlugin.getGames();
        Optional<Game> templateArena = arenas.stream().filter((game -> game.getGameArenaData().getName().equals(templateArenaName))).findAny();
        templateArena.ifPresent((ta) -> {
            String newWorldName = UUID.randomUUID().toString().substring(0,8);
            mvPlugin.getMVWorldManager().cloneWorld(ta.getGameArenaData().getBound().getWorld().getName(), newWorldName);
            logger.warning("World copied");
        });
    }
}
