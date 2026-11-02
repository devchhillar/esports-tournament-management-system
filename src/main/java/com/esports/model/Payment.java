package com.esports.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model representing a Payment or Financial Transaction (Entry Fee, Prize Payout, Refund).
 */
public class Payment {
    private int id;
    private int tournamentId;
    private String tournamentTitle;
    private int teamId;
    private String teamName;
    private String teamTag;
    private int userId;
    private String userName;
    private BigDecimal amount = BigDecimal.ZERO;
    private String paymentType = "ENTRY_FEE"; // ENTRY_FEE, PRIZE_PAYOUT, REFUND
    private String paymentMethod = "UPI";     // UPI, CREDIT_CARD, NET_BANKING, WALLET, OFFLINE
    private String transactionId;
    private String status = "COMPLETED";      // PENDING, COMPLETED, FAILED, REFUNDED
    private Timestamp paymentDate;
    private String notes;

    public Payment() {}

    public Payment(int tournamentId, int teamId, int userId, BigDecimal amount,
                   String paymentType, String paymentMethod, String transactionId, String status, String notes) {
        this.tournamentId = tournamentId;
        this.teamId = teamId;
        this.userId = userId;
        this.amount = amount;
        this.paymentType = paymentType;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.status = status;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }

    public String getTournamentTitle() { return tournamentTitle; }
    public void setTournamentTitle(String tournamentTitle) { this.tournamentTitle = tournamentTitle; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getTeamTag() { return teamTag; }
    public void setTeamTag(String teamTag) { this.teamTag = teamTag; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setAmount(double amount) { this.amount = BigDecimal.valueOf(amount); }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Payment #" + id + " [" + transactionId + "] ₹" + amount + " (" + status + ")";
    }
}
