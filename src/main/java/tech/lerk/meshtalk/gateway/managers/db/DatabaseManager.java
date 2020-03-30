package tech.lerk.meshtalk.gateway.managers.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Utility class that manages database operations.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
public class DatabaseManager {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private final Connection connection;
    private final MessageDatabaseManager messageDatabaseManager;
    private final HandshakeDatabaseManager handshakeDatabaseManager;

    public DatabaseManager(String url, String user, String password) throws SQLException {
        log.info("Connecting to database...");
        connection = DriverManager.getConnection(url, user, password);
        handshakeDatabaseManager = new HandshakeDatabaseManager(connection);
        messageDatabaseManager = new MessageDatabaseManager(connection);
    }

    public MessageDatabaseManager getMessageDatabaseManager() {
        return messageDatabaseManager;
    }

    public HandshakeDatabaseManager getHandshakeDatabaseManager() {
        return handshakeDatabaseManager;
    }

    public static class NoResultException extends SQLException {
        public NoResultException(String message) {
            super(message);
        }
    }
}
