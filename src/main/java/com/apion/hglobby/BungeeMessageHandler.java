package com.apion.hglobby;

import com.apion.hglobby.matchmake.QueueManager;
import com.google.common.io.ByteArrayDataInput;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLogger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BungeeMessageHandler {
    private static final PluginLogger logger = (PluginLogger) Bukkit.getLogger();
    private QueueManager queueManager;
    public BungeeMessageHandler(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public void handleMessage(ByteArrayDataInput message) {
        String messageType = message.readUTF();
        switch (messageType) {
            case "PlayerCount" -> {
                String serverName = message.readUTF();
                int playerCount = message.readInt();
                Optional.of(queueManager.getPlayerCountTasks().get(serverName)).ifPresent((list) -> {
                    for (CompletableFuture<Pair<String, Integer>> future : list) {
                        future.complete(new ImmutablePair<String, Integer>(serverName, playerCount));
                    }
                });
            }
            case "GetServers" -> {
                String serverList = message.readUTF();
                queueManager.getGetServersTasks().forEach((task) -> {
                    task.complete(serverList);
                });
            }
            default -> {
                logger.warning("Received a message with type " + messageType + " , but had nothing to do");
            }
        }
    }
}
