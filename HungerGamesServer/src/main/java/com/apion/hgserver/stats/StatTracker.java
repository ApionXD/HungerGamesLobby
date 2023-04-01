package com.apion.hgserver.stats;

import com.apion.hgserver.HungerGamesServer;
import com.apion.hgserver.database.ExecuteSqlCommand;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import tk.shanebee.hg.events.FreeRoamEvent;
import tk.shanebee.hg.events.GameStartEvent;
import tk.shanebee.hg.events.PlayerDeathGameEvent;
import tk.shanebee.hg.game.Game;

import java.util.List;
import java.util.UUID;

public class StatTracker implements Listener {

    /**
     * Listen for a death in a game event and add it to the database
     * @param event PlayerDeathGameEvent fired by HungerGames
     */
    @EventHandler
    public void onPlayerKill(PlayerDeathGameEvent event) {
        // Only execute if death was because of a player
        if (event.getEntity().getLastDamageCause() == null && (
                event.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                event.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                )
        ) {
            return;
        }
        final Entity killerEntity = event.getEntity().getLastDamageCause().getEntity();
        if (!(killerEntity instanceof final Player killer)) {
            return;
        }
        final Player killed = event.getEntity();
        final String server = HungerGamesServer.getInstance().getConfig().getString("bungee.serverName");
        final Game game = event.getGame();
        final String arenaName = game.getGameArenaData().getName();
        String primaryGroup = null;
        final User luckPermsUser = LuckPermsProvider.get().getUserManager().getUser(killer.getUniqueId());
        if (luckPermsUser != null) {
            primaryGroup = luckPermsUser.getPrimaryGroup();
        }

        /*
         Track a kill on a specific server
         */
        ExecuteSqlCommand.insertPlayerKill(
                killer.getUniqueId(),
                killed.getUniqueId(),
                server,
                arenaName,
                primaryGroup
        );
        ExecuteSqlCommand.insertPlayerPlacement(
                killed.getUniqueId(),
                server,
                arenaName,
                event.getGame().getGamePlayerData().getPlayers().size(),
                event.getGame().getGamePlayerData().getAllPlayers().size(),
                primaryGroup
        );
    }
    @EventHandler
    public void onGameStart(FreeRoamEvent event) {
        List<UUID> players = event.getGame().getGamePlayerData().getPlayers();
        String arenaName = event.getGame().getGameArenaData().getName();


        players.forEach(player -> {
            final User luckPermsUser = LuckPermsProvider.get().getUserManager().getUser(player);
            String primaryGroup = null;
            if (luckPermsUser != null) {
                primaryGroup = luckPermsUser.getPrimaryGroup();
            }
            ExecuteSqlCommand.insertArenaPlayer(arenaName, String.valueOf(player), primaryGroup);
        });
    }

}
