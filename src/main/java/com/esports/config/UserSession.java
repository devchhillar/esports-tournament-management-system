package com.esports.config;

import com.esports.model.Role;
import com.esports.model.User;

/**
 * Thread-safe In-Memory Session Manager.
 * Stores information about the currently authenticated user during the application's runtime.
 */
public class UserSession {

    private static UserSession instance;
    private User currentUser;
    private Integer playerId; // Cached player id if current user is a PLAYER

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Authenticate and initialize a session.
     */
    public synchronized void login(User user) {
        this.currentUser = user;
        this.playerId = null;
    }

    /**
     * Clears the current active session.
     */
    public synchronized void logout() {
        this.currentUser = null;
        this.playerId = null;
    }

    public synchronized boolean isLoggedIn() {
        return this.currentUser != null;
    }

    public synchronized User getCurrentUser() {
        return this.currentUser;
    }

    public synchronized Integer getPlayerId() {
        return this.playerId;
    }

    public synchronized void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public synchronized boolean hasRole(Role role) {
        return this.currentUser != null && this.currentUser.getRole() == role;
    }
}
