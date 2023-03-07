package com.apion.hglobby.matchmake;

import com.apion.hglobby.HungerGamesLobby;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class QueueCommand implements CommandExecutor {
    private static final String COMMAND_NAME = "queue";
    private static final String COMMAND_DESCRIPTION = "Enters you into a queue for Hunger Games";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            HungerGamesLobby.queueManager.registerIntoQueue(((Player) sender));
        }
        else {
            sender.sendMessage("Could not register you into queue because you are not a player.");
        }
        return true;
    }
}
