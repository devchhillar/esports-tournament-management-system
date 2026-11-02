package com.esports.model;

/**
 * Model representing a Tournament Standing / Leaderboard entry.
 */
public class TournamentStanding {
    private int id;
    private int tournamentId;
    private int teamId;
    private String teamName;
    private String teamTag;
    private String groupName;
    private int matchesPlayed;
    private int wins;
    private int losses;
    private int draws;
    private int points;
    private int scoreDiff;
    private int rankPosition;

    public TournamentStanding() {}

    public TournamentStanding(int tournamentId, int teamId, String teamName, String teamTag, 
                              String groupName, int matchesPlayed, int wins, int losses, 
                              int draws, int points, int scoreDiff, int rankPosition) {
        this.tournamentId = tournamentId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamTag = teamTag;
        this.groupName = groupName;
        this.matchesPlayed = matchesPlayed;
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
        this.points = points;
        this.scoreDiff = scoreDiff;
        this.rankPosition = rankPosition;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getTeamTag() { return teamTag; }
    public void setTeamTag(String teamTag) { this.teamTag = teamTag; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getScoreDiff() { return scoreDiff; }
    public void setScoreDiff(int scoreDiff) { this.scoreDiff = scoreDiff; }

    public int getRankPosition() { return rankPosition; }
    public void setRankPosition(int rankPosition) { this.rankPosition = rankPosition; }
}
