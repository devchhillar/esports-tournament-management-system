package com.esports.dao;

import com.esports.config.DBConnection;
import com.esports.model.GameCategory;
import com.esports.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamDAOImpl implements TeamDAO {

    private static final Logger logger = LoggerFactory.getLogger(TeamDAOImpl.class);

    @Override
    public boolean create(Team team, int creatorUserId) {
        String insertTeamSql = "INSERT INTO teams (team_name, team_tag, leader_id, manager_id, game, logo_path, description, status) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String selectPlayerSql = "SELECT id FROM players WHERE user_id = ?";
        String insertMemberSql = "INSERT INTO team_members (team_id, player_id, role) VALUES (?, ?, 'CAPTAIN')";
        String updatePlayerTeamSql = "UPDATE players SET current_team_id = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int generatedTeamId = 0;
            try (PreparedStatement psTeam = conn.prepareStatement(insertTeamSql, Statement.RETURN_GENERATED_KEYS)) {
                psTeam.setString(1, team.getTeamName());
                psTeam.setString(2, team.getTeamTag());
                psTeam.setInt(3, creatorUserId);
                if (team.getManagerId() != null) {
                    psTeam.setInt(4, team.getManagerId());
                } else {
                    psTeam.setNull(4, Types.INTEGER);
                }
                psTeam.setString(5, team.getGame());
                psTeam.setString(6, team.getLogoPath());
                psTeam.setString(7, team.getDescription());
                psTeam.setString(8, team.getStatus() != null ? team.getStatus() : "ACTIVE");

                int affected = psTeam.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet keys = psTeam.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedTeamId = keys.getInt(1);
                        team.setId(generatedTeamId);
                    }
                }
            }

            // Find player id of this user
            int playerId = 0;
            try (PreparedStatement psPlayer = conn.prepareStatement(selectPlayerSql)) {
                psPlayer.setInt(1, creatorUserId);
                try (ResultSet rs = psPlayer.executeQuery()) {
                    if (rs.next()) {
                        playerId = rs.getInt("id");
                    }
                }
            }

            // If player profile exists, add them as captain in team_members and update current_team_id
            if (playerId > 0 && generatedTeamId > 0) {
                try (PreparedStatement psMember = conn.prepareStatement(insertMemberSql)) {
                    psMember.setInt(1, generatedTeamId);
                    psMember.setInt(2, playerId);
                    psMember.executeUpdate();
                }
                try (PreparedStatement psUp = conn.prepareStatement(updatePlayerTeamSql)) {
                    psUp.setInt(1, generatedTeamId);
                    psUp.setInt(2, playerId);
                    psUp.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error creating team: {}", e.getMessage(), e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean update(Team team) {
        String sql = "UPDATE teams SET team_name = ?, team_tag = ?, game = ?, logo_path = ?, " +
                     "description = ?, status = ?, manager_id = ?, ranking_points = ?, " +
                     "matches_played = ?, matches_won = ?, matches_lost = ?, total_points = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getTeamTag());
            ps.setString(3, team.getGame());
            ps.setString(4, team.getLogoPath());
            ps.setString(5, team.getDescription());
            ps.setString(6, team.getStatus());
            if (team.getManagerId() != null) {
                ps.setInt(7, team.getManagerId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, team.getRankingPoints());
            ps.setInt(9, team.getMatchesPlayed());
            ps.setInt(10, team.getMatchesWon());
            ps.setInt(11, team.getMatchesLost());
            ps.setInt(12, team.getTotalPoints());
            ps.setInt(13, team.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating team {}: {}", team.getId(), e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM teams WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting team {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Team findById(int id) {
        String sql = "SELECT t.*, u.full_name AS leader_name, m.full_name AS manager_name, " +
                     "(SELECT COUNT(*) FROM team_members WHERE team_id = t.id) AS member_count " +
                     "FROM teams t " +
                     "JOIN users u ON t.leader_id = u.id " +
                     "LEFT JOIN users m ON t.manager_id = m.id " +
                     "WHERE t.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding team by id ({}): {}", id, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Team> findByLeaderId(int leaderId) {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT t.*, u.full_name AS leader_name, m.full_name AS manager_name, " +
                     "(SELECT COUNT(*) FROM team_members WHERE team_id = t.id) AS member_count " +
                     "FROM teams t " +
                     "JOIN users u ON t.leader_id = u.id " +
                     "LEFT JOIN users m ON t.manager_id = m.id " +
                     "WHERE t.leader_id = ? ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, leaderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding teams by leader: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Team> findByManagerId(int managerId) {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT t.*, u.full_name AS leader_name, m.full_name AS manager_name, " +
                     "(SELECT COUNT(*) FROM team_members WHERE team_id = t.id) AS member_count " +
                     "FROM teams t " +
                     "JOIN users u ON t.leader_id = u.id " +
                     "LEFT JOIN users m ON t.manager_id = m.id " +
                     "WHERE t.manager_id = ? ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding teams by manager: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Team> findByUserId(int userId) {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT DISTINCT t.*, u.full_name AS leader_name, m.full_name AS manager_name, " +
                     "(SELECT COUNT(*) FROM team_members WHERE team_id = t.id) AS member_count " +
                     "FROM teams t " +
                     "JOIN users u ON t.leader_id = u.id " +
                     "LEFT JOIN users m ON t.manager_id = m.id " +
                     "LEFT JOIN team_members tm ON t.id = tm.team_id " +
                     "LEFT JOIN players p ON tm.player_id = p.id " +
                     "WHERE t.leader_id = ? OR t.manager_id = ? OR p.user_id = ? " +
                     "ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding teams by user ({}): {}", userId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Team> findAll() {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT t.*, u.full_name AS leader_name, m.full_name AS manager_name, " +
                     "(SELECT COUNT(*) FROM team_members WHERE team_id = t.id) AS member_count " +
                     "FROM teams t " +
                     "JOIN users u ON t.leader_id = u.id " +
                     "LEFT JOIN users m ON t.manager_id = m.id " +
                     "ORDER BY t.ranking_points DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all teams: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Team> findByGame(String game) {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT t.*, u.full_name AS leader_name, m.full_name AS manager_name, " +
                     "(SELECT COUNT(*) FROM team_members WHERE team_id = t.id) AS member_count " +
                     "FROM teams t " +
                     "JOIN users u ON t.leader_id = u.id " +
                     "LEFT JOIN users m ON t.manager_id = m.id " +
                     (game != null && !game.equalsIgnoreCase("ALL") ? "WHERE LOWER(t.game) = LOWER(?) " : "") +
                     "ORDER BY t.ranking_points DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (game != null && !game.equalsIgnoreCase("ALL")) {
                ps.setString(1, game);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding teams by game: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Team> findByGame(GameCategory game) {
        return findByGame(game != null ? game.getDisplayName() : "ALL");
    }

    @Override
    public boolean addMember(int teamId, int playerId, String role) {
        // First check roster limit & eligibility
        if (!isPlayerEligibleForTeam(playerId, teamId)) {
            logger.warn("Player {} is not eligible or team {} roster is full.", playerId, teamId);
            return false;
        }

        String insertSql = "INSERT INTO team_members (team_id, player_id, role) VALUES (?, ?, ?)";
        String updatePlayerSql = "UPDATE players SET current_team_id = ? WHERE id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(insertSql)) {
                ps1.setInt(1, teamId);
                ps1.setInt(2, playerId);
                String memberRole = "MEMBER";
                if (role != null) {
                    String rUpper = role.trim().toUpperCase();
                    if (rUpper.equals("CAPTAIN") || rUpper.equals("SUBSTITUTE")) {
                        memberRole = rUpper;
                    }
                }
                ps1.setString(3, memberRole);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(updatePlayerSql)) {
                ps2.setInt(1, teamId);
                ps2.setInt(2, playerId);
                ps2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error adding player to team: {}", e.getMessage(), e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean removeMember(int teamId, int playerId) {
        String deleteSql = "DELETE FROM team_members WHERE team_id = ? AND player_id = ?";
        String updatePlayerSql = "UPDATE players SET current_team_id = NULL WHERE id = ? AND current_team_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(deleteSql)) {
                ps1.setInt(1, teamId);
                ps1.setInt(2, playerId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(updatePlayerSql)) {
                ps2.setInt(1, playerId);
                ps2.setInt(2, teamId);
                ps2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error removing player from team: {}", e.getMessage(), e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean assignCaptain(int teamId, int playerId) {
        String clearSql = "UPDATE team_members SET role = 'MEMBER' WHERE team_id = ? AND role = 'CAPTAIN'";
        String setSql = "UPDATE team_members SET role = 'CAPTAIN' WHERE team_id = ? AND player_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(clearSql)) {
                ps1.setInt(1, teamId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(setSql)) {
                ps2.setInt(1, teamId);
                ps2.setInt(2, playerId);
                ps2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error assigning captain: {}", e.getMessage(), e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean assignManager(int teamId, int managerUserId) {
        String sql = "UPDATE teams SET manager_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerUserId);
            ps.setInt(2, teamId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error assigning team manager: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public int getMemberCount(int teamId) {
        String sql = "SELECT COUNT(*) FROM team_members WHERE team_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting member count: {}", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int getMaxRosterSize(int teamId) {
        String sql = "SELECT g.max_team_size FROM teams t " +
                     "JOIN games g ON LOWER(t.game) = LOWER(g.name) WHERE t.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ignored) {}
        return 6; // default fallback
    }

    @Override
    public boolean isPlayerEligibleForTeam(int playerId, int teamId) {
        // 1. Check current roster size vs max allowed
        int currentSize = getMemberCount(teamId);
        int maxSize = getMaxRosterSize(teamId);
        if (currentSize >= maxSize) {
            return false;
        }

        // 2. Check if player already in this team
        String checkSql = "SELECT COUNT(*) FROM team_members WHERE team_id = ? AND player_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
        } catch (SQLException e) {
            return false;
        }

        // 3. Check game compatibility (player primary game matches team game or is 'Other')
        String compatSql = "SELECT p.primary_game, t.game FROM players p, teams t WHERE p.id = ? AND t.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(compatSql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String pGame = rs.getString("primary_game");
                    String tGame = rs.getString("game");
                    if (pGame != null && tGame != null && !pGame.equalsIgnoreCase("Other")) {
                        return pGame.equalsIgnoreCase(tGame);
                    }
                }
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM teams";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting teams: {}", e.getMessage(), e);
        }
        return 0;
    }

    private Team mapRow(ResultSet rs) throws SQLException {
        Team t = new Team();
        t.setId(rs.getInt("id"));
        t.setTeamName(rs.getString("team_name"));
        t.setTeamTag(rs.getString("team_tag"));
        t.setLeaderId(rs.getInt("leader_id"));
        int managerId = rs.getInt("manager_id");
        if (!rs.wasNull()) {
            t.setManagerId(managerId);
        }
        t.setGame(rs.getString("game"));
        t.setLogoPath(rs.getString("logo_path"));
        t.setDescription(rs.getString("description"));
        t.setStatus(rs.getString("status"));
        t.setRankingPoints(rs.getInt("ranking_points"));
        t.setMatchesPlayed(rs.getInt("matches_played"));
        t.setMatchesWon(rs.getInt("matches_won"));
        t.setMatchesLost(rs.getInt("matches_lost"));
        t.setTotalPoints(rs.getInt("total_points"));
        t.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            t.setLeaderName(rs.getString("leader_name"));
            t.setManagerName(rs.getString("manager_name"));
            t.setMemberCount(rs.getInt("member_count"));
        } catch (SQLException ignored) {}

        return t;
    }
}
