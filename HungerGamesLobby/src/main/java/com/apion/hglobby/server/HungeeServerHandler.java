package com.apion.hglobby.server;

import com.apion.hglobby.HungerGamesLobby;
import com.apion.hungeeshared.enums.ChannelNames;

public class HungeeServerHandler {
    public void init() {
        final HungerGamesLobby instance = HungerGamesLobby.getInstance();
        instance.getServer().getMessenger().registerOutgoingPluginChannel(instance, ChannelNames.HUNGEE_GAMES_MANAGER.channelName);
    }
}
