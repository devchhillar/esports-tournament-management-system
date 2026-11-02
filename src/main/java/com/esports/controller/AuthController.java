package com.esports.controller;

import com.esports.config.PasswordUtil;
import com.esports.config.UserSession;
import com.esports.dao.*;
import com.esports.model.Player;
import com.esports.model.Role;
import com.esports.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Controller responsible for Authentication (Login, Registration, Logout, Session).
 */
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserDAO userDAO;
    private final PlayerDAO playerDAO;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public AuthController() {
        this.userDAO = new UserDAOImpl();
        this.playerDAO = new PlayerDAOImpl();
    }

    public AuthController(UserDAO userDAO, PlayerDAO playerDAO) {
        this.userDAO = userDAO;
        this.playerDAO = playerDAO;
    }

    /**
     * Authenticates a user with username and password.
     */
    public AuthResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Username is required.", null);
        }
        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Password is required.", null);
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return new AuthResult(false, "Invalid username or password.", null);
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return new AuthResult(false, "Account is " + user.getStatus().toLowerCase() + ". Please contact administrator.", null);
        }

        boolean passwordMatches = PasswordUtil.checkPassword(password, user.getPasswordHash());
        if (!passwordMatches) {
            userDAO.recordLoginHistory(user.getId(), "127.0.0.1", "FAILED_PASSWORD");
            return new AuthResult(false, "Invalid username or password.", null);
        }

        // Initialize Session
        UserSession.getInstance().login(user);
        userDAO.recordLoginHistory(user.getId(), "127.0.0.1", "SUCCESS");
        userDAO.updateLastLogin(user.getId());

        // Cache player ID if user is a Player
        if (user.getRole() == Role.PLAYER) {
            Player p = playerDAO.findByUserId(user.getId());
            if (p != null) {
                UserSession.getInstance().setPlayerId(p.getId());
            }
        }

        logger.info("User {} successfully logged in as {}", user.getUsername(), user.getRole());
        return new AuthResult(true, "Login successful! Welcome " + user.getFullName() + ".", user);
    }

    /**
     * Registers a new user with validation and password hashing.
     */
    public AuthResult register(String username, String email, String password, String confirmPassword, 
                               Role role, String fullName, String phone) {
        // Validations
        if (username == null || username.trim().length() < 3) {
            return new AuthResult(false, "Username must be at least 3 characters long.", null);
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return new AuthResult(false, "Please provide a valid email address.", null);
        }
        if (password == null || password.length() < 6) {
            return new AuthResult(false, "Password must be at least 6 characters long.", null);
        }
        if (!password.equals(confirmPassword)) {
            return new AuthResult(false, "Passwords do not match.", null);
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            return new AuthResult(false, "Full name cannot be empty.", null);
        }
        if (role == null) {
            return new AuthResult(false, "Please select a user role.", null);
        }

        String cleanUsername = username.trim();
        String cleanEmail = email.trim();

        if (userDAO.isUsernameTaken(cleanUsername)) {
            return new AuthResult(false, "Username '" + cleanUsername + "' is already taken. Please choose another.", null);
        }
        if (userDAO.isEmailTaken(cleanEmail)) {
            return new AuthResult(false, "Email '" + cleanEmail + "' is already registered.", null);
        }

        // BCrypt encryption
        String hashedPassword = PasswordUtil.hashPassword(password);
        User newUser = new User(cleanUsername, cleanEmail, hashedPassword, role, fullName.trim(), phone != null ? phone.trim() : "");

        boolean created = userDAO.createUser(newUser);
        if (created) {
            // If user is a player, create their default Player profile
            if (role == Role.PLAYER && newUser.getId() > 0) {
                Player newPlayer = new Player();
                newPlayer.setUserId(newUser.getId());
                newPlayer.setGamerTag(cleanUsername.toUpperCase() + "_OP");
                newPlayer.setBio("Competitive esports athlete.");
                newPlayer.setPrimaryGame("Valorant");
                newPlayer.setInGameRole("Assault / Fragger");
                newPlayer.setCountry("India");
                newPlayer.setRankingPoints(1000);
                playerDAO.createPlayer(newPlayer);
            }

            logger.info("Registered new user {} with role {}", newUser.getUsername(), role);
            return new AuthResult(true, "Registration successful! You can now log in.", newUser);
        } else {
            return new AuthResult(false, "Registration failed due to a database error. Please verify your connection.", null);
        }
    }

    /**
     * Terminate user session.
     */
    public void logout() {
        UserSession.getInstance().logout();
    }

    /**
     * Helper Result Class
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;

        public AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }
    }
}
