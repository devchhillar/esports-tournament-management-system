package com.esports.dao;

import com.esports.config.DBConnection;
import com.esports.model.Registration;
import com.esports.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAOImpl implements RegistrationDAO {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationDAOImpl.class);

    @Override
    public boolean registerTeam(int tournamentId, int teamId, int userId) {
        String sql = "INSERT INTO registrations (tournament_id, team_id, registered_by, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tournamentId);
            ps.setInt(2, teamId);
            ps.setInt(3, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error registering team for tournament: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateStatus(int registrationId, String status) {
        String sql = "UPDATE registrations SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, registrationId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating registration status: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<Registration> findAll() {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT r.*, t.title AS tournament_title, t.game AS game_name, " +
                     "tm.team_name, tm.team_tag, u.full_name AS registered_by_name " +
                     "FROM registrations r " +
                     "JOIN tournaments t ON r.tournament_id = t.id " +
                     "JOIN teams tm ON r.team_id = tm.id " +
                     "JOIN users u ON r.registered_by = u.id " +
                     "ORDER BY r.registration_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all registrations: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Registration> findByTournament(int tournamentId) {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT r.*, t.title AS tournament_title, t.game AS game_name, " +
                     "tm.team_name, tm.team_tag, u.full_name AS registered_by_name " +
                     "FROM registrations r " +
                     "JOIN tournaments t ON r.tournament_id = t.id " +
                     "JOIN teams tm ON r.team_id = tm.id " +
                     "JOIN users u ON r.registered_by = u.id " +
                     "WHERE r.tournament_id = ? ORDER BY r.registration_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding registrations by tournament ({}): {}", tournamentId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Registration> findByTeam(int teamId) {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT r.*, t.title AS tournament_title, t.game AS game_name, " +
                     "tm.team_name, tm.team_tag, u.full_name AS registered_by_name " +
                     "FROM registrations r " +
                     "JOIN tournaments t ON r.tournament_id = t.id " +
                     "JOIN teams tm ON r.team_id = tm.id " +
                     "JOIN users u ON r.registered_by = u.id " +
                     "WHERE r.team_id = ? ORDER BY r.registration_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding registrations by team ({}): {}", teamId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Team> getApprovedTeams(int tournamentId) {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT tm.*, u.full_name AS leader_name, m.full_name AS manager_name, " +
                     "(SELECT COUNT(*) FROM team_members WHERE team_id = tm.id) AS member_count " +
                     "FROM registrations r " +
                     "JOIN teams tm ON r.team_id = tm.id " +
                     "JOIN users u ON tm.leader_id = u.id " +
                     "LEFT JOIN users m ON tm.manager_id = m.id " +
                     "WHERE r.tournament_id = ? AND r.status = 'APPROVED' " +
                     "ORDER BY tm.ranking_points DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Team t = new Team();
                    t.setId(rs.getInt("id"));
                    t.setTeamName(rs.getString("team_name"));
                    t.setTeamTag(rs.getString("team_tag"));
                    t.setLeaderId(rs.getInt("leader_id"));
                    t.setGame(rs.getString("game"));
                    t.setRankingPoints(rs.getInt("ranking_points"));
                    t.setMatchesPlayed(rs.getInt("matches_played"));
                    t.setMatchesWon(rs.getInt("matches_won"));
                    t.setMatchesLost(rs.getInt("matches_lost"));
                    t.setTotalPoints(rs.getInt("total_points"));
                    t.setLeaderName(rs.getString("leader_name"));
                    t.setManagerName(rs.getString("manager_name"));
                    t.setMemberCount(rs.getInt("member_count"));
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting approved teams for tournament {}: {}", tournamentId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean isAlreadyRegistered(int tournamentId, int teamId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE tournament_id = ? AND team_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tournamentId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking registration status: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM registrations WHERE status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting pending registrations: {}", e.getMessage(), e);
        }
        return 0;
    }

    private Registration mapRow(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setId(rs.getInt("id"));
        r.setTournamentId(rs.getInt("tournament_id"));
        r.setTeamId(rs.getInt("team_id"));
        r.setRegisteredBy(rs.getInt("registered_by"));
        r.setStatus(rs.getString("status"));
        r.setRegistrationDate(rs.getTimestamp("registration_date"));

        try {
            r.setTournamentTitle(rs.getString("tournament_title"));
            r.setGameName(rs.getString("game_name"));
            r.setTeamName(rs.getString("team_name"));
            r.setTeamTag(rs.getString("team_tag"));
            r.setRegisteredByName(rs.getString("registered_by_name"));
        } catch (SQLException ignored) {}

        return r;
    }
}
