package com.esports.dao;

import com.esports.config.DBConnection;
import com.esports.model.GameCategory;
import com.esports.model.Tournament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentDAOImpl implements TournamentDAO {

    private static final Logger logger = LoggerFactory.getLogger(TournamentDAOImpl.class);

    @Override
    public boolean create(Tournament t) {
        String sql = "INSERT INTO tournaments (title, game, description, banner_path, logo_path, format, " +
                     "min_teams, max_teams, reg_open_date, reg_close_date, start_date, end_date, " +
                     "entry_fee, prize_pool, first_prize, second_prize, third_prize, organizer, rules, status, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getTitle());
            ps.setString(2, t.getGame());
            ps.setString(3, t.getDescription());
            ps.setString(4, t.getBannerPath());
            ps.setString(5, t.getLogoPath());
            ps.setString(6, t.getFormat() != null ? t.getFormat() : "SINGLE_ELIMINATION");
            ps.setInt(7, t.getMinTeams());
            ps.setInt(8, t.getMaxTeams());
            ps.setTimestamp(9, t.getRegOpenDate());
            ps.setTimestamp(10, t.getRegCloseDate());
            ps.setTimestamp(11, t.getStartDate() != null ? t.getStartDate() : new Timestamp(System.currentTimeMillis() + 86400000L));
            ps.setTimestamp(12, t.getEndDate() != null ? t.getEndDate() : new Timestamp(System.currentTimeMillis() + 86400000L * 7));
            ps.setBigDecimal(13, t.getEntryFee());
            ps.setBigDecimal(14, t.getPrizePool());
            ps.setBigDecimal(15, t.getFirstPrize());
            ps.setBigDecimal(16, t.getSecondPrize());
            ps.setBigDecimal(17, t.getThirdPrize());
            ps.setString(18, t.getOrganizer());
            ps.setString(19, t.getRules());
            ps.setString(20, t.getStatus() != null ? t.getStatus() : "UPCOMING");
            ps.setInt(21, t.getCreatedBy() > 0 ? t.getCreatedBy() : 1);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        t.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating tournament: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean update(Tournament t) {
        String sql = "UPDATE tournaments SET title = ?, game = ?, description = ?, banner_path = ?, logo_path = ?, " +
                     "format = ?, min_teams = ?, max_teams = ?, reg_open_date = ?, reg_close_date = ?, " +
                     "start_date = ?, end_date = ?, entry_fee = ?, prize_pool = ?, first_prize = ?, " +
                     "second_prize = ?, third_prize = ?, organizer = ?, rules = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getTitle());
            ps.setString(2, t.getGame());
            ps.setString(3, t.getDescription());
            ps.setString(4, t.getBannerPath());
            ps.setString(5, t.getLogoPath());
            ps.setString(6, t.getFormat());
            ps.setInt(7, t.getMinTeams());
            ps.setInt(8, t.getMaxTeams());
            ps.setTimestamp(9, t.getRegOpenDate());
            ps.setTimestamp(10, t.getRegCloseDate());
            ps.setTimestamp(11, t.getStartDate() != null ? t.getStartDate() : new Timestamp(System.currentTimeMillis() + 86400000L));
            ps.setTimestamp(12, t.getEndDate() != null ? t.getEndDate() : new Timestamp(System.currentTimeMillis() + 86400000L * 7));
            ps.setBigDecimal(13, t.getEntryFee());
            ps.setBigDecimal(14, t.getPrizePool());
            ps.setBigDecimal(15, t.getFirstPrize());
            ps.setBigDecimal(16, t.getSecondPrize());
            ps.setBigDecimal(17, t.getThirdPrize());
            ps.setString(18, t.getOrganizer());
            ps.setString(19, t.getRules());
            ps.setString(20, t.getStatus());
            ps.setInt(21, t.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating tournament {}: {}", t.getId(), e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean updateStatus(int id, String newStatus) {
        String sql = "UPDATE tournaments SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating tournament status ({}): {}", id, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM tournaments WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting tournament {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Tournament findById(int id) {
        String sql = "SELECT t.*, " +
                     "(SELECT COUNT(*) FROM registrations WHERE tournament_id = t.id AND status = 'APPROVED') AS reg_count " +
                     "FROM tournaments t WHERE t.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding tournament by id ({}): {}", id, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Tournament> findAll() {
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT t.*, " +
                     "(SELECT COUNT(*) FROM registrations WHERE tournament_id = t.id AND status = 'APPROVED') AS reg_count " +
                     "FROM tournaments t ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all tournaments: {}", e.getMessage(), e);
        }
        return list;
    }

    private boolean isAllFilter(String value, String... allVariants) {
        if (value == null) return true;
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("ALL")) return true;
        for (String v : allVariants) {
            if (trimmed.equalsIgnoreCase(v)) return true;
        }
        return false;
    }

    @Override
    public List<Tournament> findByGame(String game) {
        if (isAllFilter(game, "All Games", "All Game")) {
            return findAll();
        }
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT t.*, " +
                     "(SELECT COUNT(*) FROM registrations WHERE tournament_id = t.id AND status = 'APPROVED') AS reg_count " +
                     "FROM tournaments t WHERE LOWER(t.game) = LOWER(?) " +
                     "ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, game.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding tournaments by game: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Tournament> findByGame(GameCategory game) {
        return findByGame(game != null ? game.getDisplayName() : "ALL");
    }

    @Override
    public List<Tournament> findByStatus(String status) {
        if (isAllFilter(status, "All Statuses", "All Status")) {
            return findAll();
        }
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT t.*, " +
                     "(SELECT COUNT(*) FROM registrations WHERE tournament_id = t.id AND status = 'APPROVED') AS reg_count " +
                     "FROM tournaments t WHERE t.status = ? ORDER BY t.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding tournaments by status: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Tournament> search(String keyword, String game, String status) {
        List<Tournament> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.*, " +
                "(SELECT COUNT(*) FROM registrations WHERE tournament_id = t.id AND status = 'APPROVED') AS reg_count " +
                "FROM tournaments t WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(t.title) LIKE ? OR LOWER(t.organizer) LIKE ? OR CAST(t.id AS CHAR) LIKE ? OR LOWER(t.game) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (game != null && !isAllFilter(game, "All Games", "All Game")) {
            sql.append("AND LOWER(t.game) = LOWER(?) ");
            params.add(game.trim());
        }
        if (status != null && !isAllFilter(status, "All Statuses", "All Status")) {
            sql.append("AND t.status = ? ");
            params.add(status.trim().toUpperCase());
        }
        sql.append("ORDER BY t.created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching tournaments: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM tournaments";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting tournaments: {}", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countActive() {
        String sql = "SELECT COUNT(*) FROM tournaments WHERE status IN ('ONGOING', 'REGISTRATION_OPEN')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting active tournaments: {}", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM tournaments WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting tournaments by status: {}", e.getMessage(), e);
        }
        return 0;
    }

    private Tournament mapRow(ResultSet rs) throws SQLException {
        Tournament t = new Tournament();
        t.setId(rs.getInt("id"));
        t.setTitle(rs.getString("title"));
        t.setGame(rs.getString("game"));
        t.setDescription(rs.getString("description"));
        t.setBannerPath(rs.getString("banner_path"));
        t.setLogoPath(rs.getString("logo_path"));
        t.setFormat(rs.getString("format"));
        t.setMinTeams(rs.getInt("min_teams"));
        t.setMaxTeams(rs.getInt("max_teams"));
        t.setRegOpenDate(rs.getTimestamp("reg_open_date"));
        t.setRegCloseDate(rs.getTimestamp("reg_close_date"));
        t.setStartDate(rs.getTimestamp("start_date"));
        t.setEndDate(rs.getTimestamp("end_date"));
        t.setEntryFee(rs.getBigDecimal("entry_fee"));
        t.setPrizePool(rs.getBigDecimal("prize_pool"));
        t.setFirstPrize(rs.getBigDecimal("first_prize"));
        t.setSecondPrize(rs.getBigDecimal("second_prize"));
        t.setThirdPrize(rs.getBigDecimal("third_prize"));
        t.setOrganizer(rs.getString("organizer"));
        t.setRules(rs.getString("rules"));
        t.setStatus(rs.getString("status"));
        t.setCreatedBy(rs.getInt("created_by"));
        t.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            t.setRegisteredTeamsCount(rs.getInt("reg_count"));
        } catch (SQLException ignored) {}

        return t;
    }
}
