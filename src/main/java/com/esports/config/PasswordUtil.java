package com.esports.config;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords securely using BCrypt.
 */
public class PasswordUtil {

    // Work factor of 12 provides strong protection against brute-force attacks
    private static final int LOG_ROUNDS = 12;

    /**
     * Hash a plain-text password using BCrypt with a randomly generated salt.
     * @param plainPassword The plain text password
     * @return The 60-character BCrypt hashed string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verify a plain-text password against a stored BCrypt hash.
     * @param plainPassword Candidate plain text password
     * @param hashedPassword Stored BCrypt hashed string
     * @return true if password matches, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid BCrypt hash format: " + e.getMessage());
            return false;
        }
    }
}
