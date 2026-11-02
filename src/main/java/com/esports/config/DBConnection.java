package com.esports.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-safe Database Connection Manager.
 * Reads database credentials from db.properties and supplies active JDBC connections.
 */
public class DBConnection {

    private static final Properties properties = new Properties();
    private static String url;
    private static String user;
    private static String password;
    private static String driver;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("Warning: db.properties not found on classpath. Falling back to default settings.");
                url = "jdbc:mysql://localhost:3306/esports_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                user = "root";
                password = "root";
                driver = "com.mysql.cj.jdbc.Driver";
            } else {
                properties.load(input);
                url = properties.getProperty("db.url");
                user = properties.getProperty("db.user");
                password = properties.getProperty("db.password");
                driver = properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            }

            Class.forName(driver);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error initializing Database Connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtains a fresh database connection.
     * @return Connection instance
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests if the database connection can be established successfully.
     * @return ConnectionStatus result with details
     */
    public static ConnectionStatus testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                String catalog = conn.getCatalog();
                String serverVersion = conn.getMetaData().getDatabaseProductVersion();
                return new ConnectionStatus(true, "Successfully connected to MySQL database: " + catalog + " (Version: " + serverVersion + ")");
            }
            return new ConnectionStatus(false, "Connection is closed or null.");
        } catch (SQLException e) {
            return new ConnectionStatus(false, "Database connection failed: " + e.getMessage());
        }
    }

    public static class ConnectionStatus {
        private final boolean success;
        private final String message;

        public ConnectionStatus(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
