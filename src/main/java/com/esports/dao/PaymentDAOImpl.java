package com.esports.dao;

import com.esports.config.DBConnection;
import com.esports.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDAOImpl.class);

    private String baseQuery() {
        return "SELECT p.*, t.title AS tournament_title, tm.team_name, tm.team_tag, u.full_name AS user_name " +
               "FROM payments p " +
               "JOIN tournaments t ON p.tournament_id = t.id " +
               "JOIN teams tm ON p.team_id = tm.id " +
               "JOIN users u ON p.user_id = u.id";
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setTournamentId(rs.getInt("tournament_id"));
        p.setTournamentTitle(rs.getString("tournament_title"));
        p.setTeamId(rs.getInt("team_id"));
        p.setTeamName(rs.getString("team_name"));
        p.setTeamTag(rs.getString("team_tag"));
        p.setUserId(rs.getInt("user_id"));
        p.setUserName(rs.getString("user_name"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentType(rs.getString("payment_type"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setStatus(rs.getString("status"));
        p.setPaymentDate(rs.getTimestamp("payment_date"));
        p.setNotes(rs.getString("notes"));
        return p;
    }

    @Override
    public boolean recordPayment(Payment payment) {
        String sql = "INSERT INTO payments (tournament_id, team_id, user_id, amount, payment_type, payment_method, transaction_id, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, payment.getTournamentId());
            ps.setInt(2, payment.getTeamId());
            ps.setInt(3, payment.getUserId());
            ps.setBigDecimal(4, payment.getAmount());
            ps.setString(5, payment.getPaymentType());
            ps.setString(6, payment.getPaymentMethod());
            ps.setString(7, payment.getTransactionId());
            ps.setString(8, payment.getStatus());
            ps.setString(9, payment.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        payment.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error recording payment: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Payment findById(int id) {
        String sql = baseQuery() + " WHERE p.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("Error finding payment by id {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Payment findByTransactionId(String transactionId) {
        String sql = baseQuery() + " WHERE p.transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("Error finding payment by transaction {}: {}", transactionId, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>();
        String sql = baseQuery() + " ORDER BY p.payment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("Error listing all payments: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Payment> findByTournament(int tournamentId) {
        List<Payment> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE p.tournament_id = ? ORDER BY p.payment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding payments by tournament {}: {}", tournamentId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Payment> findByTeam(int teamId) {
        List<Payment> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE p.team_id = ? ORDER BY p.payment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding payments by team {}: {}", teamId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Payment> findByUser(int userId) {
        List<Payment> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE p.user_id = ? ORDER BY p.payment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding payments by user {}: {}", userId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Payment> findByStatus(String status) {
        List<Payment> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE p.status = ? ORDER BY p.payment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding payments by status {}: {}", status, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean updateStatus(int paymentId, String status) {
        String sql = "UPDATE payments SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating payment status: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_type = 'ENTRY_FEE' AND status = 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            logger.error("Error calculating total revenue: {}", e.getMessage(), e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalPrizePayouts() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_type = 'PRIZE_PAYOUT' AND status = 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            logger.error("Error calculating total prize payouts: {}", e.getMessage(), e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isTeamPaid(int tournamentId, int teamId) {
        String sql = "SELECT COUNT(*) FROM payments WHERE tournament_id = ? AND team_id = ? AND payment_type = 'ENTRY_FEE' AND status = 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking payment status: {}", e.getMessage(), e);
        }
        return false;
    }
}
