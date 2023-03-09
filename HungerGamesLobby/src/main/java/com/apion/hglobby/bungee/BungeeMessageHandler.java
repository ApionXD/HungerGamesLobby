package com.apion.hglobby.bungee;

import com.apion.hglobby.enums.BungeeMessageTypes;
import com.google.common.io.ByteArrayDataInput;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLogger;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class BungeeMessageHandler {
    private static final Logger logger = Bukkit.getLogger();

    private static final Queue<Pair<String, CompletableFuture<Object>>> futures = new LinkedList<>();

    public static void putNewFuture(final String messageType, final CompletableFuture<Object> future) {
        futures.add(Pair.of(messageType, future));
    }

    public void handleMessage(ByteArrayDataInput message) {
        String messageType = message.readUTF();
        // filter the queue to only the futures with the correct message type
        // then get the next future in line for completion
        final Pair<String, CompletableFuture<Object>> nextInLine = futures.stream().filter(
                        (element) -> element.getKey().equals(messageType)
                )
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("No futures in line for messageType " + messageType));
        final CompletableFuture<Object> future = nextInLine.getValue();
        switch (messageType) {
            case "PlayerCount" -> {
                String serverName = message.readUTF();
                int playerCount = message.readInt();
                final Pair<String, Integer> serverNamePlayerCountPair = Pair.of(serverName, playerCount);
                future.complete(serverNamePlayerCountPair);
            }
            case "GetServers" -> {
                List<String> serverList = Arrays.stream(message.readUTF().split(", ")).toList();
                future.complete(serverList);
            }
            default -> logger.warning("Received a message with type " + messageType + " , but had nothing to do");
        }
    }
}
