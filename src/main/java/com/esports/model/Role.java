package com.esports.model;

/**
 * User Roles supported in the eSports Management System.
 */
public enum Role {
    ADMIN("Administrator"),
    TEAM_MANAGER("Team Manager"),
    PLAYER("Player / Gamer"),
    VIEWER("Public Spectator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null) return VIEWER;
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return VIEWER;
        }
    }
}
