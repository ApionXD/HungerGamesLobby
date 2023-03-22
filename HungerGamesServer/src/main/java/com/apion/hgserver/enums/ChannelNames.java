package com.apion.hgserver.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ChannelNames {
    BUNGEE("BungeeCord"),
    HUNGEE_GAMES_MANAGER("bungeegames:main");

    public final String channelName;
}
