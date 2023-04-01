package com.apion.hgserver.command;

import com.apion.hgserver.HungerGamesServer;
import com.apion.hgserver.runnables.MovePlayersToMainServerRunnable;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.PlayerData;

import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

public class GoToLobbyCommand implements CommandExecutor {
    private final Logger logger = Bukkit.getLogger();
    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            final Player player = ((Player) sender).getPlayer();
            final HG hg = HungerGamesServer.getHgPlugin();
            PlayerData playerData = hg.getPlayerManager().getPlayerData(player);

            if (Objects.isNull(playerData)) {
                playerData = hg.getPlayerManager().getSpectatorData(player);
                if (Objects.isNull(player)) {
                    sender.sendMessage("You aren't a spectator or a player. Can't make you leave an arena!");
                    return true;
                }
            }
            if (player == null || playerData == null) {
                logger.severe("Player or PlayerData was null when they were attempting to leave an arena");
                return false;
            }

            playerData.getGame().getGamePlayerData().leave(player, false);
            new MovePlayersToMainServerRunnable(Collections.singletonList(player.getUniqueId())).runTask(HungerGamesServer.getInstance());
        }

        return true;
    }
}
