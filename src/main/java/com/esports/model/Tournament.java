package com.esports.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model representing an eSports Tournament.
 */
public class Tournament {
    private int id;
    private String title;
    private String game = "Valorant";
    private String description;
    private String bannerPath;
    private String logoPath;
    private String format = "SINGLE_ELIMINATION";
    private int minTeams = 2;
    private int maxTeams = 16;
    private Timestamp regOpenDate;
    private Timestamp regCloseDate;
    private Timestamp startDate;
    private Timestamp endDate;
    private BigDecimal entryFee = BigDecimal.ZERO;
    private BigDecimal prizePool = BigDecimal.ZERO;
    private BigDecimal firstPrize = BigDecimal.ZERO;
    private BigDecimal secondPrize = BigDecimal.ZERO;
    private BigDecimal thirdPrize = BigDecimal.ZERO;
    private String organizer = "eSports Arena Global";
    private String rules;
    private String status = "UPCOMING";
    private int createdBy;
    private Timestamp createdAt;
    
    // Display helpers
    private int registeredTeamsCount;

    public Tournament() {}

    public Tournament(int id, String title, String game, String description, BigDecimal prizePool, 
                      BigDecimal entryFee, int maxTeams, Timestamp startDate, Timestamp endDate, 
                      String status, int createdBy) {
        this.id = id;
        this.title = title;
        this.game = game;
        this.description = description;
        this.prizePool = prizePool;
        this.entryFee = entryFee;
        this.maxTeams = maxTeams;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdBy = createdBy;
    }

    public Tournament(int id, String title, GameCategory gameCategory, String description, BigDecimal prizePool, 
                      BigDecimal entryFee, int maxTeams, Timestamp startDate, Timestamp endDate, 
                      String status, int createdBy) {
        this.id = id;
        this.title = title;
        this.game = gameCategory != null ? gameCategory.getDisplayName() : "Other";
        this.description = description;
        this.prizePool = prizePool;
        this.entryFee = entryFee;
        this.maxTeams = maxTeams;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdBy = createdBy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public void setGame(GameCategory gameCategory) { 
        this.game = gameCategory != null ? gameCategory.getDisplayName() : "Other"; 
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBannerPath() { return bannerPath; }
    public void setBannerPath(String bannerPath) { this.bannerPath = bannerPath; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public int getMinTeams() { return minTeams; }
    public void setMinTeams(int minTeams) { this.minTeams = minTeams; }

    public int getMaxTeams() { return maxTeams; }
    public void setMaxTeams(int maxTeams) { this.maxTeams = maxTeams; }

    public Timestamp getRegOpenDate() { return regOpenDate; }
    public void setRegOpenDate(Timestamp regOpenDate) { this.regOpenDate = regOpenDate; }

    public Timestamp getRegCloseDate() { return regCloseDate; }
    public void setRegCloseDate(Timestamp regCloseDate) { this.regCloseDate = regCloseDate; }

    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }

    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }

    public BigDecimal getEntryFee() { return entryFee; }
    public void setEntryFee(BigDecimal entryFee) { this.entryFee = entryFee; }
    public void setEntryFee(double entryFee) { this.entryFee = BigDecimal.valueOf(entryFee); }

    public BigDecimal getPrizePool() { return prizePool; }
    public void setPrizePool(BigDecimal prizePool) { this.prizePool = prizePool; }
    public void setPrizePool(double prizePool) { this.prizePool = BigDecimal.valueOf(prizePool); }

    public BigDecimal getFirstPrize() { return firstPrize; }
    public void setFirstPrize(BigDecimal firstPrize) { this.firstPrize = firstPrize; }

    public BigDecimal getSecondPrize() { return secondPrize; }
    public void setSecondPrize(BigDecimal secondPrize) { this.secondPrize = secondPrize; }

    public BigDecimal getThirdPrize() { return thirdPrize; }
    public void setThirdPrize(BigDecimal thirdPrize) { this.thirdPrize = thirdPrize; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getRegisteredTeamsCount() { return registeredTeamsCount; }
    public void setRegisteredTeamsCount(int registeredTeamsCount) { this.registeredTeamsCount = registeredTeamsCount; }

    @Override
    public String toString() {
        return title + " (" + game + " - " + format + ")";
    }
}
