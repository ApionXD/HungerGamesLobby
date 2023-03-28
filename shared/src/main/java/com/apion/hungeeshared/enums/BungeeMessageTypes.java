package com.apion.hungeeshared.enums;

public enum BungeeMessageTypes {
    GET_SERVERS("GetServers"),
    PLAYER_COUNT("PlayerCount");
    public final String messageType;

    BungeeMessageTypes(String messageType) {
        this.messageType = messageType;
    }
}
