package com.lostfound.member3_matching.dao;

import com.lostfound.config.DatabaseConnection;
import com.lostfound.model.Item;
import com.lostfound.model.MatchResult;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO implementation for dynamic Search queries and Match scoring persistence.
 * Uses PreparedStatements dynamically and securely.
 */
public class SearchDAOImpl {

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getLong("item_id"));
        item.setUserId(rs.getLong("user_id"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setTitle(rs.getString("title"));
        item.setDescription(rs.getString("description"));
        item.setItemType(rs.getString("item_type"));
        item.setLocation(rs.getString("location"));
        item.setLatitude(rs.getDouble("latitude"));
        item.setLongitude(rs.getDouble("longitude"));
        item.setItemDate(rs.getDate("item_date"));
        item.setImage(rs.getString("image"));
        item.setStatus(rs.getString("status"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setCategoryName(rs.getString("category_name"));
        item.setUserName(rs.getString("user_name"));
        item.setUserEmail(rs.getString("user_email"));
        item.setUserPhone(rs.getString("user_phone"));
        return item;
    }

    /**
     * Executes dynamic search with multiple combined filters.
     */
    public List<Item> searchItems(String keyword, Integer categoryId, String location,
                                  String itemType, Date itemDate, String status) throws SQLException {
        List<Item> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT i.item_id, i.user_id, i.category_id, i.title, i.description, " +
                "i.item_type, i.location, i.latitude, i.longitude, i.item_date, i.image, " +
                "i.status, i.created_at, c.name AS category_name, u.name AS user_name, " +
                "u.email AS user_email, u.phone AS user_phone " +
                "FROM items i " +
                "JOIN categories c ON i.category_id = c.category_id " +
                "JOIN users u ON i.user_id = u.user_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(i.title) LIKE ? OR LOWER(i.description) LIKE ? OR LOWER(i.location) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND i.category_id = ? ");
            params.add(categoryId);
        }

        if (location != null && !location.trim().isEmpty()) {
            sql.append("AND LOWER(i.location) LIKE ? ");
            params.add("%" + location.trim().toLowerCase() + "%");
        }

        if (itemType != null && !itemType.trim().isEmpty() && !"ALL".equalsIgnoreCase(itemType)) {
            sql.append("AND i.item_type = ? ");
            params.add(itemType.trim().toUpperCase());
        }

        if (itemDate != null) {
            sql.append("AND i.item_date = ? ");
            params.add(itemDate);
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND i.status = ? ");
            params.add(status.trim().toUpperCase());
        } else {
            sql.append("AND i.status != 'CLOSED' ");
        }

        sql.append("ORDER BY i.created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToItem(rs));
                }
            }
        }

        return results;
    }

    /**
     * Saves a calculated match result into the database.
     */
    public Long saveMatch(MatchResult match) throws SQLException {
        // Delete any existing match between these two items to prevent duplicate records
        String deleteSql = "DELETE FROM matches WHERE lost_item_id = ? AND found_item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
            delPs.setLong(1, match.getLostItemId());
            delPs.setLong(2, match.getFoundItemId());
            delPs.executeUpdate();
        }

        String insertSql = "INSERT INTO matches (lost_item_id, found_item_id, text_score, location_score, " +
                           "date_score, category_score, total_score) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, match.getLostItemId());
            ps.setLong(2, match.getFoundItemId());
            ps.setDouble(3, match.getTextScore());
            ps.setDouble(4, match.getLocationScore());
            ps.setDouble(5, match.getDateScore());
            ps.setDouble(6, match.getCategoryScore());
            ps.setDouble(7, match.getTotalScore());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves recorded matches for a given item (either lost or found).
     */
    public List<MatchResult> getMatchesForItem(Long itemId) throws SQLException {
        List<MatchResult> list = new ArrayList<>();
        String sql = "SELECT m.* FROM matches m " +
                     "WHERE m.lost_item_id = ? OR m.found_item_id = ? " +
                     "ORDER BY m.total_score DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itemId);
            ps.setLong(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchResult res = new MatchResult();
                    res.setMatchId(rs.getLong("match_id"));
                    res.setLostItemId(rs.getLong("lost_item_id"));
                    res.setFoundItemId(rs.getLong("found_item_id"));
                    res.setTextScore(rs.getDouble("text_score"));
                    res.setLocationScore(rs.getDouble("location_score"));
                    res.setDateScore(rs.getDouble("date_score"));
                    res.setCategoryScore(rs.getDouble("category_score"));
                    res.setTotalScore(rs.getDouble("total_score"));
                    res.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(res);
                }
            }
        }
        return list;
    }
}
