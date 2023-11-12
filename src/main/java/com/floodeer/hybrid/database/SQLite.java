package com.floodeer.hybrid.database;

import com.floodeer.hybrid.Hybrid;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class SQLite extends DatabaseProvider<HikariDataSource> {

    public SQLite() throws SQLException {
        super(Executors.newCachedThreadPool());
    }

    @Override
    protected HikariDataSource initSource() {

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + Hybrid.get().getDataFolder() + "/players.db");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        Hybrid.get().getLogger().info("Started SQLite.");

        return new HikariDataSource(hikariConfig);
    }

    @Override
    protected void createTables() throws SQLException {
        try (Connection connection = getSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS `defensor_player` ( " +
                             "`player_id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                             "`uuid` VARCHAR(255) NOT NULL UNIQUE, " +
                             "`playername` VARCHAR(60) NOT NULL, " +
                             "`wins` INT NOT NULL DEFAULT 0, " +
                             "`losses` INT NOT NULL DEFAULT 0, " +
                             "`games_played` INT NOT NULL DEFAULT 0, " +
                             "`wave_record` INT NOT NULL DEFAULT 0, " +
                             "`kills` INT NOT NULL DEFAULT 0, " +
                             "`damage_caused` DOUBLE NOT NULL DEFAULT 0, " +
                             "`balance` INT NOT NULL DEFAULT 0, " +
                             "`exp` INT NOT NULL DEFAULT 0, " +
                             "`rank` VARCHAR(60) NOT NULL DEFAULT 'Level-1', " +
                             "`kit` VARCHAR(60) NOT NULL DEFAULT 'builder', " +
                             "`kits` VARCHAR(9999) NOT NULL DEFAULT 'builder');"
             )) {
            preparedStatement.execute();
        }
    }

    @Override
    protected void close(HikariDataSource source) {
        source.close();
    }
}
