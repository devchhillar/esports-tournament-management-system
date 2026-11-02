package com.esports.model;

import java.sql.Timestamp;

/**
 * Model representing an eSports Game Title.
 */
public class Game {
    private int id;
    private String name;
    private String category;
    private String description;
    private String logoPath;
    private int minTeamSize;
    private int maxTeamSize;
    private String status;
    private Timestamp createdAt;

    public Game() {
        this.status = "ACTIVE";
        this.minTeamSize = 1;
        this.maxTeamSize = 5;
    }

    public Game(int id, String name, String category, String description, String logoPath, 
                int minTeamSize, int maxTeamSize, String status, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.logoPath = logoPath;
        this.minTeamSize = minTeamSize;
        this.maxTeamSize = maxTeamSize;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Game(String name, String category, String description, int minTeamSize, int maxTeamSize) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.minTeamSize = minTeamSize;
        this.maxTeamSize = maxTeamSize;
        this.status = "ACTIVE";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public int getMinTeamSize() {
        return minTeamSize;
    }

    public void setMinTeamSize(int minTeamSize) {
        this.minTeamSize = minTeamSize;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(int maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name + " (" + category + ")";
    }
}
