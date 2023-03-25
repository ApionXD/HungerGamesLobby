package com.apion.hgserver.database;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class ExecuteSqlCommand {
    private final static Logger logger = Bukkit.getLogger();
    private final static String insertPlayerKillSql = //language=sql
            "Insert Into player_kills(time,                player_killer, player_killed, server, arena_id, primary_group ) " +
            "                 Values (current_timestamp(), ?            , ?            , ?    ,  ?       ,             ? ) " ;

    public static boolean insertPlayerKill(
            final UUID playerKillerUuid,
            final UUID playerKilledUuid,
            final String server,
            final String arenaId,
            final String primary_group
    ) {
        final Connection con = Database.getConnection();
        try {
            final PreparedStatement statement = con.prepareStatement(insertPlayerKillSql);
            statement.setString(1, playerKillerUuid.toString());
            statement.setString(2, playerKilledUuid.toString());
            statement.setString(3, server);
            statement.setString(4, arenaId);
            statement.setString(5, primary_group);
            statement.execute();
            return true;
        } catch (SQLException e) {
            logger.severe("Could not insert player kill: ");
            e.printStackTrace();
        }
        return false;
    }
}
