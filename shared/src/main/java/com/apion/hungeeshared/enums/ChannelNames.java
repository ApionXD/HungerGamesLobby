package com.apion.hungeeshared.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ChannelNames {
    BUNGEE("BungeeCord"),
    HUNGEE_GAMES_MANAGER("bungeegames:main");

    public final String channelName;
}
