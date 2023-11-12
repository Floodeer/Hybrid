package com.floodeer.hybrid.database;

import lombok.Getter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

public abstract class DatabaseProvider <T extends DataSource> {

    private final T source;

    @Getter
    private final ExecutorService executor;

    public DatabaseProvider(ExecutorService executor) throws SQLException {
        this.source = initSource();
        this.executor = executor;
        testConnection(source);

        createTables();
    }

    protected abstract T initSource();

    public final void shutdown() {
        close(source);
    }

    public DataSource getSource() {
        return source;
    }

    public Connection getConnection() {
        try {
            return source.getConnection();
        }catch(SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    protected abstract void close(T source);

    protected abstract void createTables() throws SQLException;

    protected boolean testConnection(DataSource source) throws SQLException {
        try (Connection conn = source.getConnection()) {
            return conn.isValid(5 * 1000);
        }
    }
}
