package server;

import com.apion.hungeeshared.enums.ChannelNames;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@NoArgsConstructor
public class HungeeServerExecutor {
    private static final Logger logger = Bukkit.getLogger();
    public void writeStringMessage(
            String serverName,
            String messageText,
            Plugin plugin
    ) {
        ByteArrayDataOutput message = ByteStreams.newDataOutput();
        message.writeUTF("Forward");
        // To server
        message.writeUTF(serverName);
        // What channel are we listening on
        message.writeUTF(ChannelNames.HUNGEE_GAMES_MANAGER.channelName);
        byte[] messageContents = messageText.getBytes(StandardCharsets.UTF_8);
        message.writeShort(messageContents.length);
        message.write(messageContents);
        Player player = Bukkit.getOnlinePlayers().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No player online to forward"));
        player.sendPluginMessage(plugin, "BungeeCord", message.toByteArray());
    }

    public void sendInitArenaMessage(
            final String serverName,
            final String arenaName,
            final Plugin plugin
    ) {
        writeStringMessage(serverName,  "InitArena " + arenaName, plugin);
    }

    public void sendMovePlayerMessage(
            final String serverName,
            final Player player,
            final Plugin plugin
    ) {
        ByteArrayDataOutput message = ByteStreams.newDataOutput();
        message.writeUTF("Connect");
        message.writeUTF(serverName);
        player.sendPluginMessage(
                plugin,
                ChannelNames.BUNGEE.channelName,
                message.toByteArray()
        );
    }

    public void sendArenaMoveMessage(
            final String serverName,
            final String arenaName,
            final Collection<UUID> playerUuids,
            final Plugin plugin
    ) {
        StringBuilder messageText = new StringBuilder();
        messageText.append("ArenaMove ");
        messageText.append(arenaName).append(" ");
        messageText.append(playerUuids.stream().map(UUID::toString).collect(Collectors.joining(",")));
        writeStringMessage(serverName, messageText.toString(), plugin);
    }
}
