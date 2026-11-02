package com.esports;

import com.esports.config.PasswordUtil;
import com.esports.controller.AuthController;
import com.esports.dao.UserDAO;
import com.esports.dao.UserDAOImpl;
import com.esports.model.LoginHistory;
import com.esports.model.Role;
import com.esports.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authentication and Security Test Suite")
public class AuthAndSecurityTest {

    private UserDAO userDAO;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAOImpl();
        authController = new AuthController();
    }

    @Test
    @DisplayName("Should properly hash and verify passwords using BCrypt")
    void testBCryptHashing() {
        String rawPassword = "SuperSecretPassword123!";
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hashedPassword, "Hashed password must not be null");
        assertNotEquals(rawPassword, hashedPassword, "Hash must never match raw password");
        assertTrue(hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$"),
                "Hash should be a standard BCrypt hash string");

        assertTrue(PasswordUtil.checkPassword(rawPassword, hashedPassword),
                "PasswordUtil.checkPassword should return true for correct raw password");
        assertFalse(PasswordUtil.checkPassword("WrongPassword", hashedPassword),
                "PasswordUtil.checkPassword should return false for incorrect raw password");
    }

    @Test
    @DisplayName("Should successfully authenticate admin seed user via AuthController")
    void testAdminAuthentication() {
        AuthController.AuthResult result = authController.login("admin", "admin123");
        assertTrue(result.isSuccess(), "Admin user 'admin' with password 'admin123' should be authenticated successfully: " + result.getMessage());

        User admin = result.getUser();
        assertNotNull(admin);
        assertEquals("admin", admin.getUsername());
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals("ACTIVE", admin.getStatus());
    }

    @Test
    @DisplayName("Should reject authentication with invalid password")
    void testInvalidPasswordAuthentication() {
        AuthController.AuthResult result = authController.login("admin", "WrongPassword999");
        assertFalse(result.isSuccess(), "Authentication with invalid password must fail");
        assertNull(result.getUser(), "User object should be null on failed login");
    }

    @Test
    @DisplayName("Should reject authentication with non-existent user")
    void testNonExistentUserAuthentication() {
        AuthController.AuthResult result = authController.login("non_existent_user_9999", "admin123");
        assertFalse(result.isSuccess(), "Authentication for non-existent user must fail");
        assertNull(result.getUser());
    }

    @Test
    @DisplayName("Should create new user with hashed password and verify credentials and login history")
    void testUserRegistrationAndLookup() {
        String testUsername = "test_user_" + System.currentTimeMillis();
        String rawPassword = "TestPassword@2026";
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);

        User newUser = new User();
        newUser.setUsername(testUsername);
        newUser.setPasswordHash(hashedPassword);
        newUser.setEmail(testUsername + "@testesports.com");
        newUser.setFullName("Test User QA");
        newUser.setRole(Role.PLAYER);
        newUser.setStatus("ACTIVE");

        boolean created = userDAO.createUser(newUser);
        assertTrue(created, "User creation should succeed");
        assertTrue(newUser.getId() > 0, "Created user ID should be a positive integer");

        // Verify via AuthController
        AuthController.AuthResult authResult = authController.login(testUsername, rawPassword);
        assertTrue(authResult.isSuccess(), "Newly registered user should authenticate successfully");
        assertEquals(Role.PLAYER, authResult.getUser().getRole());
        assertEquals("Test User QA", authResult.getUser().getFullName());

        // Test login history record and last login update
        userDAO.updateLastLogin(newUser.getId());
        userDAO.recordLoginHistory(newUser.getId(), "127.0.0.1", "SUCCESS");

        List<LoginHistory> history = userDAO.getLoginHistory(newUser.getId());
        assertNotNull(history);
        assertFalse(history.isEmpty(), "Login history must record the successful test attempt");
        assertEquals("SUCCESS", history.get(0).getStatus());

        // Test password change
        String newPassword = "NewUpdatedPassword#2026";
        String newHash = PasswordUtil.hashPassword(newPassword);
        boolean passUpdated = userDAO.updatePassword(newUser.getId(), newHash);
        assertTrue(passUpdated, "Password update should return true");

        // Old password should now fail
        AuthController.AuthResult oldAuth = authController.login(testUsername, rawPassword);
        assertFalse(oldAuth.isSuccess(), "Old password should no longer work");

        // New password should succeed
        AuthController.AuthResult newAuth = authController.login(testUsername, newPassword);
        assertTrue(newAuth.isSuccess(), "New password should authenticate successfully");
    }
}
