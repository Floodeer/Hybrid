package com.floodeer.hybrid.database;

import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SQLWriter {

    private static final int NUM_OF_ATTEMPTS = 5;

    public abstract void onWrite() throws SQLException;

    public void writeOperation(final Executor executor, final Logger logger, final String exceptionMessage) {
        executor.execute(() -> attemptWrites(logger, exceptionMessage));
    }

    protected void attemptWrites(final Logger logger, final String exceptionMessage) {
        for (int attempt = 1; attempt <= NUM_OF_ATTEMPTS; ++attempt) {
            try {
                onWrite();
                return;
            } catch (SQLException e) {
                if (attempt == NUM_OF_ATTEMPTS) {
                    logger.log(Level.SEVERE, exceptionMessage, e);
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
