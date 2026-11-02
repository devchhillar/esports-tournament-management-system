package com.esports.dao;

import com.esports.config.DBConnection;
import com.esports.model.TournamentStanding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentStandingDAOImpl implements TournamentStandingDAO {

    private static final Logger logger = LoggerFactory.getLogger(TournamentStandingDAOImpl.class);

    @Override
    public List<TournamentStanding> getStandingsByTournament(int tournamentId) {
        recalculateRanks(tournamentId);
        List<TournamentStanding> list = new ArrayList<>();
        String sql = "SELECT s.*, t.team_name, t.team_tag FROM tournament_standings s " +
                     "JOIN teams t ON s.team_id = t.id " +
                     "WHERE s.tournament_id = ? ORDER BY s.points DESC, s.score_diff DESC, s.wins DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    TournamentStanding ts = new TournamentStanding(
                            rs.getInt("tournament_id"),
                            rs.getInt("team_id"),
                            rs.getString("team_name"),
                            rs.getString("team_tag"),
                            rs.getString("group_name"),
                            rs.getInt("matches_played"),
                            rs.getInt("wins"),
                            rs.getInt("losses"),
                            rs.getInt("draws"),
                            rs.getInt("points"),
                            rs.getInt("score_diff"),
                            rank++
                    );
                    ts.setId(rs.getInt("id"));
                    list.add(ts);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching standings for tournament {}: {}", tournamentId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TournamentStanding> getStandingsByGroup(int tournamentId, String groupName) {
        List<TournamentStanding> list = new ArrayList<>();
        String sql = "SELECT s.*, t.team_name, t.team_tag FROM tournament_standings s " +
                     "JOIN teams t ON s.team_id = t.id " +
                     "WHERE s.tournament_id = ? AND s.group_name = ? " +
                     "ORDER BY s.points DESC, s.score_diff DESC, s.wins DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ps.setString(2, groupName);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    TournamentStanding ts = new TournamentStanding(
                            rs.getInt("tournament_id"),
                            rs.getInt("team_id"),
                            rs.getString("team_name"),
                            rs.getString("team_tag"),
                            rs.getString("group_name"),
                            rs.getInt("matches_played"),
                            rs.getInt("wins"),
                            rs.getInt("losses"),
                            rs.getInt("draws"),
                            rs.getInt("points"),
                            rs.getInt("score_diff"),
                            rank++
                    );
                    ts.setId(rs.getInt("id"));
                    list.add(ts);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching group standings: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean initializeStandings(int tournamentId, List<Integer> teamIds, String groupName) {
        String sql = "INSERT IGNORE INTO tournament_standings (tournament_id, team_id, group_name) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer teamId : teamIds) {
                ps.setInt(1, tournamentId);
                ps.setInt(2, teamId);
                ps.setString(3, groupName);
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            logger.error("Error initializing standings: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void recalculateRanks(int tournamentId) {
        String fetchSql = "SELECT id FROM tournament_standings WHERE tournament_id = ? " +
                          "ORDER BY points DESC, score_diff DESC, wins DESC";
        String updateSql = "UPDATE tournament_standings SET rank_position = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            List<Integer> ids = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setInt(1, tournamentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getInt("id"));
                    }
                }
            }
            try (PreparedStatement psUp = conn.prepareStatement(updateSql)) {
                for (int i = 0; i < ids.size(); i++) {
                    psUp.setInt(1, i + 1);
                    psUp.setInt(2, ids.get(i));
                    psUp.addBatch();
                }
                psUp.executeBatch();
            }
        } catch (SQLException e) {
            logger.error("Error recalculating ranks for tournament {}: {}", tournamentId, e.getMessage(), e);
        }
    }
}
