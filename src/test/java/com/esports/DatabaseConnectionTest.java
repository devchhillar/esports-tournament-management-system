package com.esports;

import com.esports.config.DBConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Database Connection Test Suite")
public class DatabaseConnectionTest {

    @Test
    @DisplayName("Should successfully connect to MySQL esports_management database")
    void testConnection() throws SQLException {
        DBConnection.ConnectionStatus status = DBConnection.testConnection();
        assertTrue(status.isSuccess(), "Database connection status should be success: " + status.getMessage());

        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn, "Connection object should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
            assertEquals("esports_management", conn.getCatalog(), "Connected database catalog should be esports_management");
        }
    }
}
