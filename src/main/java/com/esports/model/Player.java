package com.esports.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model representing an eSports Competitor / Player Profile.
 */
public class Player {
    private int id;
    private int userId;
    private String gamerTag;
    private String bio;
    private String profilePicture;
    private String primaryGame = "Valorant";
    private String inGameRole = "Assault / Fragger";
    private Integer currentTeamId;
    private String currentTeamName;
    private Date dob;
    private String country = "India";
    private int rankingPoints = 1000;
    private int matchesPlayed;
    private int wins;
    private int losses;
    private int kills;
    private int mvpCount;
    private Timestamp createdAt;
    
    // Joined attributes from users
    private String fullName;
    private String email;
    private String phone;

    public Player() {}

    public Player(int id, int userId, String gamerTag, String bio, String primaryGame, 
                  int rankingPoints, int matchesPlayed, int wins, int losses) {
        this.id = id;
        this.userId = userId;
        this.gamerTag = gamerTag;
        this.bio = bio;
        this.primaryGame = primaryGame;
        this.rankingPoints = rankingPoints;
        this.matchesPlayed = matchesPlayed;
        this.wins = wins;
        this.losses = losses;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getGamerTag() { return gamerTag; }
    public void setGamerTag(String gamerTag) { this.gamerTag = gamerTag; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getPrimaryGame() { return primaryGame; }
    public void setPrimaryGame(String primaryGame) { this.primaryGame = primaryGame; }
    public void setPrimaryGame(GameCategory category) { 
        this.primaryGame = category != null ? category.getDisplayName() : "Other"; 
    }

    public String getInGameRole() { return inGameRole; }
    public void setInGameRole(String inGameRole) { this.inGameRole = inGameRole; }

    public Integer getCurrentTeamId() { return currentTeamId; }
    public void setCurrentTeamId(Integer currentTeamId) { this.currentTeamId = currentTeamId; }

    public String getCurrentTeamName() { return currentTeamName; }
    public void setCurrentTeamName(String currentTeamName) { this.currentTeamName = currentTeamName; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public int getRankingPoints() { return rankingPoints; }
    public void setRankingPoints(int rankingPoints) { this.rankingPoints = rankingPoints; }

    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }

    public int getMvpCount() { return mvpCount; }
    public void setMvpCount(int mvpCount) { this.mvpCount = mvpCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getWinRate() {
        if (matchesPlayed == 0) return 0.0;
        return ((double) wins / matchesPlayed) * 100.0;
    }

    @Override
    public String toString() {
        return gamerTag + " (" + rankingPoints + " pts - " + primaryGame + ")";
    }
}
