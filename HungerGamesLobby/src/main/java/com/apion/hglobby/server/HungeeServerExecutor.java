package com.apion.hglobby.server;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hglobby.enums.ChannelNames;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

@NoArgsConstructor
public class HungeeServerExecutor {
    private static final Logger logger = Bukkit.getLogger();
    public void writeStringMessage(String serverName, String messageText) {
        logger.warning("Sending msg");
        ByteArrayDataOutput message = ByteStreams.newDataOutput();
        message.writeUTF("Forward");
        //message.writeUTF(serverName);
        //For debugging
        message.writeUTF("sg2");
        message.writeUTF(ChannelNames.HUNGEE_GAMES_MANAGER.channelName);
        byte[] messageContents = messageText.getBytes(StandardCharsets.UTF_8);
        message.writeShort(messageContents.length);
        message.write(messageContents);
        Player player = Bukkit.getOnlinePlayers().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No player online to forward"));
        player.sendPluginMessage(HungerGamesLobby.getInstance(), "BungeeCord", message.toByteArray());
    }

    public void sendInitArenaMessage(String serverName) {
        writeStringMessage(serverName, "InitArena");
    }
}
