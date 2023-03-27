package com.apion.hgserver.database;

import com.apion.hgserver.HungerGamesServer;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Database {
    private static Logger logger = Bukkit.getLogger();

    private static Connection connection;

    @SneakyThrows
    public static Connection getConnection() {
        if (connection != null && connection.isValid(1)) {
            return connection;
        }

        connection = getPluginDatasource().getConnection();
        logger.info("Refreshed database connection");

        return connection;
    }

    @SneakyThrows
    public static void initConnection() {
        connection = getPluginDatasource().getConnection();
    }

    private static MysqlDataSource getPluginDatasource() {
        final HungerGamesServer instance = HungerGamesServer.getInstance();
        // Initialize MySql connection
        final String host = instance.getConfig().getString("database.host");
        final int    port = instance.getConfig().getInt("database.port");
        final String name = instance.getConfig().getString("database.name");
        final String user = instance.getConfig().getString("database.username");
        final String pass = instance.getConfig().getString("database.password");

        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setServerName(host);
        dataSource.setPortNumber(port);
        dataSource.setDatabaseName(name);
        dataSource.setUser(user);
        dataSource.setPassword(pass);

        try (Connection con = dataSource.getConnection()) {
            if (!con.isValid(2)) {
                final String err = "Could not establish database connection";
                logger.severe(err);
                throw new SQLException(err);
            }
            logger.info("Established database connection.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dataSource;
    }
}
