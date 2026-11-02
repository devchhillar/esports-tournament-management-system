package com.esports.model;

import java.sql.Timestamp;

/**
 * Model representing User Login Activity History.
 */
public class LoginHistory {
    private int id;
    private int userId;
    private String username;
    private Timestamp loginTime;
    private String ipAddress;
    private String status;

    public LoginHistory() {}

    public LoginHistory(int id, int userId, String username, Timestamp loginTime, String ipAddress, String status) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.loginTime = loginTime;
        this.ipAddress = ipAddress;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Timestamp getLoginTime() { return loginTime; }
    public void setLoginTime(Timestamp loginTime) { this.loginTime = loginTime; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
