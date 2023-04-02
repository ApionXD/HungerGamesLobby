package com.apion.hglobby.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class QueueOther implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("hungeeLobby.queueOther")) {
            if (args.length > 0){
                Optional.ofNullable(Bukkit.getPlayer(args[0])).ifPresentOrElse(p -> {
                            Bukkit.dispatchCommand(p, "queue");
                        },
                        () -> sender.sendMessage("&cPlayer not found"));
            }
        }
        return true;
    }
}
