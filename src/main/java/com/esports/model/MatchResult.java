package com.esports.model;

import java.sql.Timestamp;

/**
 * Model representing official Match Results.
 */
public class MatchResult {
    private int id;
    private int matchId;
    private int winnerTeamId;
    private int team1Score;
    private int team2Score;
    private Integer mvpPlayerId;
    private String notes;
    private Timestamp recordedAt;

    // Display Helpers
    private String winnerTeamName;
    private String mvpPlayerTag;

    public MatchResult() {}

    public MatchResult(int id, int matchId, int winnerTeamId, int team1Score, int team2Score, Integer mvpPlayerId, String notes) {
        this.id = id;
        this.matchId = matchId;
        this.winnerTeamId = winnerTeamId;
        this.team1Score = team1Score;
        this.team2Score = team2Score;
        this.mvpPlayerId = mvpPlayerId;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public int getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(int winnerTeamId) { this.winnerTeamId = winnerTeamId; }

    public int getTeam1Score() { return team1Score; }
    public void setTeam1Score(int team1Score) { this.team1Score = team1Score; }

    public int getTeam2Score() { return team2Score; }
    public void setTeam2Score(int team2Score) { this.team2Score = team2Score; }

    public Integer getMvpPlayerId() { return mvpPlayerId; }
    public void setMvpPlayerId(Integer mvpPlayerId) { this.mvpPlayerId = mvpPlayerId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Timestamp recordedAt) { this.recordedAt = recordedAt; }

    public String getWinnerTeamName() { return winnerTeamName; }
    public void setWinnerTeamName(String winnerTeamName) { this.winnerTeamName = winnerTeamName; }

    public String getMvpPlayerTag() { return mvpPlayerTag; }
    public void setMvpPlayerTag(String mvpPlayerTag) { this.mvpPlayerTag = mvpPlayerTag; }
}
