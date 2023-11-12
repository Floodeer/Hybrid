package com.floodeer.hybrid.database;

import com.floodeer.hybrid.Hybrid;
import com.google.common.io.Resources;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.Charsets;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;

public class MySQL extends DatabaseProvider<HikariDataSource> {

    private final String connectionUri;
    private final String username;
    private final String password;

    public MySQL(String hostname, String database, String username, String password, int port) throws SQLException {
        super(Executors.newCachedThreadPool());

        this.connectionUri = String.format("jdbc:mysql://%s:%d/%s", hostname, port, database);
        this.username = username;
        this.password = password;
    }

    @Override
    protected HikariDataSource initSource() {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");

        hikariConfig.setJdbcUrl(connectionUri);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        Hybrid.get().getLogger().info("Started MySQL.");

        return new HikariDataSource(hikariConfig);
    }

    @Override
    protected void close(HikariDataSource source) {
        source.close();
    }

    @Override
    protected void createTables() throws SQLException {
        Connection connection = getSource().getConnection();
        URL resource = Resources.getResource(Hybrid.class, "/tables.sql");
        String[] databaseStructure;

        try {
            databaseStructure = Resources.toString(resource, Charsets.UTF_8).split(";");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (databaseStructure.length == 0) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            for (String query : databaseStructure) {
                query = query.trim();

                if (!query.isEmpty()) {
                    statement.execute(query);
                }
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }
}
