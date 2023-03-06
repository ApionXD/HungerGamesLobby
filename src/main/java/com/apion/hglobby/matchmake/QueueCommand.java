package com.apion.hglobby.matchmake;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class QueueCommand extends Command {
    private static final String COMMAND_NAME = "queue";
    private static final String COMMAND_DESCRIPTION = "Enters you into a queue for Hunger Games";

    public QueueCommand(String name, String description, String usageMessage, List<String> aliases) {
        super(COMMAND_NAME, COMMAND_DESCRIPTION, usageMessage, aliases);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        return false;
    }
}
