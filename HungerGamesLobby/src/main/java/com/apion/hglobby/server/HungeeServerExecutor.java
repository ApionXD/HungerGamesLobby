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

@NoArgsConstructor
public class HungeeServerExecutor {
    public void writeStringMessage(String serverName, String messageText) {
        ByteArrayDataOutput message = ByteStreams.newDataOutput();
        message.writeUTF("Forward");
        message.writeUTF(serverName);
        message.writeUTF(ChannelNames.HUNGEE_GAMES_MANAGER.channelName);
        byte[] messageContents = messageText.getBytes(StandardCharsets.UTF_8);
        message.writeInt(messageContents.length);
        message.write(messageContents);
        Optional<? extends Player> playerOpt = Bukkit.getOnlinePlayers().stream().findFirst();
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.sendPluginMessage(HungerGamesLobby.getInstance(), "BungeeCord", message.toByteArray());
        }
    }

    public void sendInitArenaMessage(String serverName) {
        writeStringMessage(serverName, "InitArena");
    }
}
