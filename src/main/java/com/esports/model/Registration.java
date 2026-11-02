package com.esports.model;

import java.sql.Timestamp;

/**
 * Model representing a Tournament Registration.
 */
public class Registration {
    private int id;
    private int tournamentId;
    private int teamId;
    private int registeredBy;
    private String status; // PENDING, APPROVED, REJECTED
    private Timestamp registrationDate;

    // Display Helpers
    private String tournamentTitle;
    private String gameName;
    private String teamName;
    private String teamTag;
    private String registeredByName;

    public Registration() {}

    public Registration(int id, int tournamentId, int teamId, int registeredBy, String status, Timestamp registrationDate) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.teamId = teamId;
        this.registeredBy = registeredBy;
        this.status = status;
        this.registrationDate = registrationDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public int getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(int registeredBy) { this.registeredBy = registeredBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Timestamp registrationDate) { this.registrationDate = registrationDate; }

    public String getTournamentTitle() { return tournamentTitle; }
    public void setTournamentTitle(String tournamentTitle) { this.tournamentTitle = tournamentTitle; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getTeamTag() { return teamTag; }
    public void setTeamTag(String teamTag) { this.teamTag = teamTag; }

    public String getRegisteredByName() { return registeredByName; }
    public void setRegisteredByName(String registeredByName) { this.registeredByName = registeredByName; }
}
