package com.esports.dao;

import com.esports.config.DBConnection;
import com.esports.model.GameCategory;
import com.esports.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAOImpl implements PlayerDAO {

    private static final Logger logger = LoggerFactory.getLogger(PlayerDAOImpl.class);

    @Override
    public Player findByUserId(int userId) {
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, t.team_name FROM players p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "LEFT JOIN teams t ON p.current_team_id = t.id " +
                     "WHERE p.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding player by userId ({}): {}", userId, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Player findById(int id) {
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, t.team_name FROM players p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "LEFT JOIN teams t ON p.current_team_id = t.id " +
                     "WHERE p.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding player by id ({}): {}", id, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Player> findAll() {
        List<Player> list = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, t.team_name FROM players p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "LEFT JOIN teams t ON p.current_team_id = t.id " +
                     "ORDER BY p.ranking_points DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all players: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Player> getLeaderboard(String game, int limit) {
        List<Player> list = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, t.team_name FROM players p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "LEFT JOIN teams t ON p.current_team_id = t.id " +
                     (game != null && !game.equalsIgnoreCase("ALL") ? "WHERE LOWER(p.primary_game) = LOWER(?) " : "") +
                     "ORDER BY p.ranking_points DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (game != null && !game.equalsIgnoreCase("ALL")) {
                ps.setString(paramIndex++, game);
            }
            ps.setInt(paramIndex, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching leaderboard: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Player> getLeaderboard(GameCategory game, int limit) {
        return getLeaderboard(game != null ? game.getDisplayName() : "ALL", limit);
    }

    @Override
    public List<Player> findByTeamId(int teamId) {
        List<Player> list = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, t.team_name FROM players p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "JOIN team_members tm ON p.id = tm.player_id " +
                     "LEFT JOIN teams t ON p.current_team_id = t.id " +
                     "WHERE tm.team_id = ? ORDER BY tm.role ASC, p.ranking_points DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding players by team ({}): {}", teamId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Player> findAvailablePlayers(String game) {
        List<Player> list = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, NULL as team_name FROM players p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.current_team_id IS NULL " +
                     (game != null ? "AND (p.primary_game = ? OR p.primary_game = 'Other') " : "") +
                     "ORDER BY p.ranking_points DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (game != null) {
                ps.setString(1, game);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding available players: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Player> findTopKillers(int limit) {
        List<Player> list = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, t.team_name FROM players p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "LEFT JOIN teams t ON p.current_team_id = t.id " +
                     "ORDER BY p.kills DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding top killers: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Player> findTopMvps(int limit) {
        List<Player> list = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name, u.email, u.phone, t.team_name FROM players p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "LEFT JOIN teams t ON p.current_team_id = t.id " +
                     "ORDER BY p.mvp_count DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding top MVPs: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean createPlayer(Player player) {
        String sql = "INSERT INTO players (user_id, gamer_tag, bio, profile_picture, primary_game, in_game_role, current_team_id, dob, country, ranking_points) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, player.getUserId());
            ps.setString(2, player.getGamerTag());
            ps.setString(3, player.getBio());
            ps.setString(4, player.getProfilePicture());
            ps.setString(5, player.getPrimaryGame());
            ps.setString(6, player.getInGameRole());
            if (player.getCurrentTeamId() != null) {
                ps.setInt(7, player.getCurrentTeamId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setDate(8, player.getDob());
            ps.setString(9, player.getCountry());
            ps.setInt(10, player.getRankingPoints());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) player.setId(gk.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating player: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateProfile(Player player) {
        String sql = "UPDATE players SET gamer_tag = ?, bio = ?, profile_picture = ?, primary_game = ?, " +
                     "in_game_role = ?, dob = ?, country = ?, current_team_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, player.getGamerTag());
            ps.setString(2, player.getBio());
            ps.setString(3, player.getProfilePicture());
            ps.setString(4, player.getPrimaryGame());
            ps.setString(5, player.getInGameRole());
            ps.setDate(6, player.getDob());
            ps.setString(7, player.getCountry());
            if (player.getCurrentTeamId() != null) {
                ps.setInt(8, player.getCurrentTeamId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setInt(9, player.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating player profile: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateRankingAndStats(int playerId, int pointsDelta, boolean won) {
        String sql = "UPDATE players SET ranking_points = GREATEST(0, ranking_points + ?), " +
                     "matches_played = matches_played + 1, " +
                     (won ? "wins = wins + 1 " : "losses = losses + 1 ") +
                     "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pointsDelta);
            ps.setInt(2, playerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating player stats: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateStats(int playerId, int matchesDelta, int winsDelta, int lossesDelta, int killsDelta, int mvpDelta, int pointsDelta) {
        String sql = "UPDATE players SET matches_played = matches_played + ?, " +
                     "wins = wins + ?, losses = losses + ?, kills = kills + ?, mvp_count = mvp_count + ?, " +
                     "ranking_points = GREATEST(0, ranking_points + ?) WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchesDelta);
            ps.setInt(2, winsDelta);
            ps.setInt(3, lossesDelta);
            ps.setInt(4, killsDelta);
            ps.setInt(5, mvpDelta);
            ps.setInt(6, pointsDelta);
            ps.setInt(7, playerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating player stats ({}): {}", playerId, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM players";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting players: {}", e.getMessage(), e);
        }
        return 0;
    }

    private Player mapRow(ResultSet rs) throws SQLException {
        Player p = new Player();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setGamerTag(rs.getString("gamer_tag"));
        p.setBio(rs.getString("bio"));
        p.setProfilePicture(rs.getString("profile_picture"));
        p.setPrimaryGame(rs.getString("primary_game"));
        p.setInGameRole(rs.getString("in_game_role"));
        int teamId = rs.getInt("current_team_id");
        if (!rs.wasNull()) {
            p.setCurrentTeamId(teamId);
        }
        p.setDob(rs.getDate("dob"));
        p.setCountry(rs.getString("country"));
        p.setRankingPoints(rs.getInt("ranking_points"));
        p.setMatchesPlayed(rs.getInt("matches_played"));
        p.setWins(rs.getInt("wins"));
        p.setLosses(rs.getInt("losses"));
        p.setKills(rs.getInt("kills"));
        p.setMvpCount(rs.getInt("mvp_count"));
        p.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            p.setFullName(rs.getString("full_name"));
            p.setEmail(rs.getString("email"));
            p.setPhone(rs.getString("phone"));
            p.setCurrentTeamName(rs.getString("team_name"));
        } catch (SQLException ignored) {}

        return p;
    }
}
