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
    private final static String insertPlayerPlacement =
            "Insert into player_placements(time,           player_uuid, server, arena_id, place, game_start_players, primary_group)" +
            "                 Values (current_timestamp(), ?          , ?     , ?       , ?    , ?                  ,?)           ";

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
    public static boolean insertPlayerPlacement(
            final UUID playerUuid,
            final String server,
            final String arenaId,
            final int place,
            final int numPlayers,
            final String group
    ) {
        final Connection con = Database.getConnection();
        try {
            final PreparedStatement statement = con.prepareStatement(insertPlayerPlacement);
            statement.setString(1, playerUuid.toString());
            statement.setString(2, server);
            statement.setString(3, arenaId);
            statement.setInt(4, place);
            statement.setInt(5, numPlayers);
            statement.setString(6, group);
            statement.execute();
            return true;
        } catch (SQLException e) {
            logger.severe("Could not insert player kill: ");
            e.printStackTrace();
        }
        return false;
    }
}
