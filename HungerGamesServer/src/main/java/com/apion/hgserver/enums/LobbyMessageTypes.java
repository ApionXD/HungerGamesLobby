package com.apion.hgserver.enums;

public enum LobbyMessageTypes {
    INIT_ARENA("ArenaInit");
    public final String messageType;

    LobbyMessageTypes(String messageType) {
        this.messageType = messageType;
    }
}
