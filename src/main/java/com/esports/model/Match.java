package com.esports.model;

import java.sql.Timestamp;

/**
 * Model representing an eSports Match fixture.
 */
public class Match {
    private int id;
    private int tournamentId;
    private String roundName;
    private int matchNumber = 1;
    private String bracketType = "KNOCKOUT"; // WINNERS, LOSERS, GRAND_FINAL, GROUP, KNOCKOUT
    private String groupName;
    private Integer team1Id;
    private Integer team2Id;
    private int team1Score;
    private int team2Score;
    private Integer winnerTeamId;
    private Timestamp scheduledTime;
    private String venue = "Online Server / Arena 1";
    private String referee = "Official Esports Referee";
    private Integer nextMatchId;
    private Integer nextMatchSlot;
    private String status = "SCHEDULED"; // SCHEDULED, LIVE, COMPLETED, CANCELLED
    private Timestamp createdAt;

    // Display Helpers
    private String tournamentTitle;
    private String gameName;
    private String team1Name;
    private String team2Name;
    private String winnerTeamName;
    private Integer mvpPlayerId;
    private String mvpPlayerName;
    private String notes;

    public Match() {}

    public Match(int id, int tournamentId, String roundName, int team1Id, int team2Id, Timestamp scheduledTime, String status) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.roundName = roundName;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
        this.scheduledTime = scheduledTime;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }

    public String getRoundName() { return roundName; }
    public void setRoundName(String roundName) { this.roundName = roundName; }

    public int getMatchNumber() { return matchNumber; }
    public void setMatchNumber(int matchNumber) { this.matchNumber = matchNumber; }

    public String getBracketType() { return bracketType; }
    public void setBracketType(String bracketType) { this.bracketType = bracketType; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Integer getTeam1Id() { return team1Id; }
    public void setTeam1Id(Integer team1Id) { this.team1Id = team1Id; }

    public Integer getTeam2Id() { return team2Id; }
    public void setTeam2Id(Integer team2Id) { this.team2Id = team2Id; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getReferee() { return referee; }
    public void setReferee(String referee) { this.referee = referee; }

    public Integer getNextMatchId() { return nextMatchId; }
    public void setNextMatchId(Integer nextMatchId) { this.nextMatchId = nextMatchId; }

    public Integer getNextMatchSlot() { return nextMatchSlot; }
    public void setNextMatchSlot(Integer nextMatchSlot) { this.nextMatchSlot = nextMatchSlot; }

    public int getTeam1Score() { return team1Score; }
    public void setTeam1Score(int team1Score) { this.team1Score = team1Score; }

    public int getTeam2Score() { return team2Score; }
    public void setTeam2Score(int team2Score) { this.team2Score = team2Score; }

    public Integer getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(Integer winnerTeamId) { this.winnerTeamId = winnerTeamId; }

    public Timestamp getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Timestamp scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getTournamentTitle() { return tournamentTitle; }
    public void setTournamentTitle(String tournamentTitle) { this.tournamentTitle = tournamentTitle; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getTeam1Name() { return team1Name != null ? team1Name : ("Team #" + team1Id); }
    public void setTeam1Name(String team1Name) { this.team1Name = team1Name; }

    public String getTeam2Name() { return team2Name != null ? team2Name : ("Team #" + team2Id); }
    public void setTeam2Name(String team2Name) { this.team2Name = team2Name; }

    public String getWinnerTeamName() { return winnerTeamName; }
    public void setWinnerTeamName(String winnerTeamName) { this.winnerTeamName = winnerTeamName; }

    public Integer getMvpPlayerId() { return mvpPlayerId; }
    public void setMvpPlayerId(Integer mvpPlayerId) { this.mvpPlayerId = mvpPlayerId; }

    public String getMvpPlayerName() { return mvpPlayerName; }
    public void setMvpPlayerName(String mvpPlayerName) { this.mvpPlayerName = mvpPlayerName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return roundName + ": " + getTeam1Name() + " vs " + getTeam2Name() + " (" + status + ")";
    }
}
