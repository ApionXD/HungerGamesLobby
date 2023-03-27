package com.apion.hgserver.arena;

import com.apion.hgserver.HungerGamesServer;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.game.Bound;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.GameArenaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class ArenaInitializer {
    private static final Logger logger = Bukkit.getLogger();
    private final HG hgPlugin = HungerGamesServer.getHgPlugin();
    private final MultiverseCore mvPlugin = HungerGamesServer.getMvPlugin();
    private final HashMap<UUID, String>  playersWaitingToBeMoved;
    public ArenaInitializer() {
        playersWaitingToBeMoved = new HashMap<>();
    }

    //Select random world
    //Create new Multiverse world from template world using multiverse clone
    //Create arena from world
    public void initializeArena(String arenaName) {
        String templateArenaName = "template1";
        List<Game> arenas = hgPlugin.getGames();
        Game arenaTemplate = arenas.stream().filter((game -> game.getGameArenaData().getName().equals(templateArenaName)))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No template arena matching " + templateArenaName));

        mvPlugin.getMVWorldManager().cloneWorld(arenaTemplate.getGameArenaData().getBound().getWorld().getName(), arenaName);
        logger.warning("World copied");
        World newWorld = Bukkit.getWorld(arenaName);

        GameArenaData ga = arenaTemplate.getGameArenaData();
        Location corner1 = ga.getBound().getGreaterCorner();
        Location corner2 = ga.getBound().getLesserCorner();
        Bound b = new Bound(arenaName, corner1.getBlockX(), corner1.getBlockY(), corner1.getBlockZ(), corner2.getBlockX(), corner2.getBlockY(), corner2.getBlockZ());
        List<Location> newSpawns  = new ArrayList<>(ga.getSpawns());
        newSpawns.forEach(l -> l.setWorld(newWorld));
        int timer = ga.getTimer();
        Game newGame = new Game(
                arenaName,
                b,
                newSpawns,
                arenaTemplate.getGameBlockData().getSign1(),
                timer,
                ga.getMinPlayers(),
                ga.getMaxPlayers(),
                ga.getRoamTime(),
                true,
                ga.getCost()
        );
        newGame.getGameArenaData().setStatus(Status.READY);
        hgPlugin.getGames().add(newGame);
    }
    public void putPlayerInArena(UUID playerUuid) {
        Player player = Optional.ofNullable(Bukkit.getPlayer(playerUuid))
                .orElseThrow(() -> {
                    logger.severe("Player " + playerUuid + " didn't exist.");
                    return new IllegalStateException("Couldn't place player in arena because they don't exist");
                });
        if (!playersWaitingToBeMoved.containsKey(playerUuid)) {
            player.sendMessage("You are not currently supposed to be in a game!");
        }

        String arenaName = playersWaitingToBeMoved.get(playerUuid);
        Game arena = hgPlugin.getGames()
                .stream()
                .filter(game -> game.getGameArenaData().getName().equals(arenaName))
                .findAny()
                .orElseThrow(
                        () -> new IllegalStateException("Couldn't find arena " + arenaName + " to place player in.")
                );
        arena.getGamePlayerData().join(player);
    }
    public void addPlayerToMap(UUID player, String arena) {
        playersWaitingToBeMoved.put(player, arena);
    }
}
