package com.apion.hgserver.arena;

import com.apion.hgserver.HungerGamesServer;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.game.Bound;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.GameArenaData;

import java.util.ArrayList;
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
            World newWorld = Bukkit.getWorld(newWorldName);

            GameArenaData ga = ta.getGameArenaData();
            Location corner1 = ga.getBound().getGreaterCorner();
            Location corner2 = ga.getBound().getLesserCorner();
            Bound b = new Bound(newWorldName, corner1.getBlockX(), corner1.getBlockY(), corner1.getBlockZ(), corner2.getBlockX(), corner2.getBlockY(), corner2.getBlockZ());
            List<Location> newSpawns  = new ArrayList<>(ga.getSpawns());
            newSpawns.forEach(l -> {
                l.setWorld(newWorld);
            });
            int timer = ga.getTimer();
            Game newGame = new Game(newWorldName, b, newSpawns, ta.getGameBlockData().getSign1(), timer, ga.getMinPlayers(), ga.getMaxPlayers(), ga.getRoamTime(), true, ga.getCost());
            newGame.getGameArenaData().setStatus(Status.READY);
        });

    }
}
