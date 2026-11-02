package com.esports.dao;

import com.esports.model.Payment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Access Object interface for Tournament Payments and Prize Payouts.
 */
public interface PaymentDAO {
    boolean recordPayment(Payment payment);
    Payment findById(int id);
    Payment findByTransactionId(String transactionId);
    List<Payment> findAll();
    List<Payment> findByTournament(int tournamentId);
    List<Payment> findByTeam(int teamId);
    List<Payment> findByUser(int userId);
    List<Payment> findByStatus(String status);
    boolean updateStatus(int paymentId, String status);
    BigDecimal getTotalRevenue();
    BigDecimal getTotalPrizePayouts();
    boolean isTeamPaid(int tournamentId, int teamId);
}
