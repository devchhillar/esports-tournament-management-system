package com.esports.model;

import java.sql.Timestamp;

/**
 * Model representing a registered User in the system.
 */
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private Role role;
    private String fullName;
    private String phone;
    private String profilePicture;
    private String status;
    private Timestamp lastLogin;
    private Timestamp createdAt;

    public User() {
    }

    public User(int id, String username, String email, String passwordHash, Role role, 
                String fullName, String phone, String profilePicture, String status, 
                Timestamp lastLogin, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.profilePicture = profilePicture;
        this.status = status;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
    }

    // Convenient constructor for registration
    public User(String username, String email, String passwordHash, Role role, String fullName, String phone) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.status = "ACTIVE";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName + " (@" + username + " - " + role + ")";
    }
}
