package com.apion.hglobby.command;

import com.apion.hglobby.HungerGamesLobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Queue implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("hungeeLobby.queue")) {
                final Player player = ((Player) sender).getPlayer();
                if (player == null) {
                    return false;
                }

                if (HungerGamesLobby.queueManager.isPlayerInQueue(player)) {
                    HungerGamesLobby.queueManager.removeFromQueueIfPresent(player);
                    sender.sendMessage(ChatColor.RED + "Removed you from the queue!");
                    return true;
                }

                HungerGamesLobby.queueManager.registerIntoQueue(((Player) sender));
                return true;
            }
            else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to queue.");
            }
        }
        else {
            sender.sendMessage("Could not register you into queue because you are not a player.");
        }
        return true;
    }
}
