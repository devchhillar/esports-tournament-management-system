package com.esports.model;

import java.sql.Timestamp;

/**
 * Model representing an eSports Team.
 */
public class Team {
    private int id;
    private String teamName;
    private String teamTag;
    private int leaderId;
    private Integer managerId;
    private String game = "Valorant";
    private String logoPath;
    private String description;
    private String status = "ACTIVE";
    private int rankingPoints = 1000;
    private int matchesPlayed;
    private int matchesWon;
    private int matchesLost;
    private int totalPoints;
    private Timestamp createdAt;
    
    // Display helpers
    private String leaderName;
    private String managerName;
    private int memberCount;

    public Team() {}

    public Team(int id, String teamName, String teamTag, int leaderId, String game) {
        this.id = id;
        this.teamName = teamName;
        this.teamTag = teamTag;
        this.leaderId = leaderId;
        this.game = game;
    }

    public Team(int id, String teamName, String teamTag, int leaderId, GameCategory gameCategory) {
        this.id = id;
        this.teamName = teamName;
        this.teamTag = teamTag;
        this.leaderId = leaderId;
        this.game = gameCategory != null ? gameCategory.getDisplayName() : "Other";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getTeamTag() { return teamTag; }
    public void setTeamTag(String teamTag) { this.teamTag = teamTag; }

    public int getLeaderId() { return leaderId; }
    public void setLeaderId(int leaderId) { this.leaderId = leaderId; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public void setGame(GameCategory gameCategory) {
        this.game = gameCategory != null ? gameCategory.getDisplayName() : "Other";
    }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRankingPoints() { return rankingPoints; }
    public void setRankingPoints(int rankingPoints) { this.rankingPoints = rankingPoints; }

    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }

    public int getMatchesWon() { return matchesWon; }
    public void setMatchesWon(int matchesWon) { this.matchesWon = matchesWon; }

    public int getMatchesLost() { return matchesLost; }
    public void setMatchesLost(int matchesLost) { this.matchesLost = matchesLost; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public double getWinRate() {
        if (matchesPlayed == 0) return 0.0;
        return ((double) matchesWon / matchesPlayed) * 100.0;
    }

    @Override
    public String toString() {
        return "[" + teamTag + "] " + teamName + " (" + game + ")";
    }
}
