package com.lostfound.member2_items.dao;

import com.lostfound.config.DatabaseConnection;
import com.lostfound.model.Category;
import com.lostfound.model.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Implementation of ItemDAO.
 */
public class ItemDAOImpl implements ItemDAO {

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

        // Join columns if present in SELECT
        try {
            item.setCategoryName(rs.getString("category_name"));
        } catch (SQLException ignored) {}
        try {
            item.setUserName(rs.getString("user_name"));
        } catch (SQLException ignored) {}
        try {
            item.setUserEmail(rs.getString("user_email"));
        } catch (SQLException ignored) {}
        try {
            item.setUserPhone(rs.getString("user_phone"));
        } catch (SQLException ignored) {}

        return item;
    }

    private static final String BASE_SELECT =
            "SELECT i.item_id, i.user_id, i.category_id, i.title, i.description, " +
            "i.item_type, i.location, i.latitude, i.longitude, i.item_date, i.image, " +
            "i.status, i.created_at, c.name AS category_name, u.name AS user_name, " +
            "u.email AS user_email, u.phone AS user_phone " +
            "FROM items i " +
            "JOIN categories c ON i.category_id = c.category_id " +
            "JOIN users u ON i.user_id = u.user_id ";

    @Override
    public Item create(Item item) throws SQLException {
        String sql = "INSERT INTO items (user_id, category_id, title, description, item_type, " +
                     "location, latitude, longitude, item_date, image, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, item.getUserId());
            ps.setInt(2, item.getCategoryId());
            ps.setString(3, item.getTitle());
            ps.setString(4, item.getDescription());
            ps.setString(5, item.getItemType());
            ps.setString(6, item.getLocation());
            ps.setDouble(7, item.getLatitude() != null ? item.getLatitude() : 0.0);
            ps.setDouble(8, item.getLongitude() != null ? item.getLongitude() : 0.0);
            ps.setDate(9, item.getItemDate());
            ps.setString(10, item.getImage());
            ps.setString(11, item.getStatus() != null ? item.getStatus() : "ACTIVE");

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Failed to insert item, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setItemId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Failed to insert item, no ID generated.");
                }
            }
            return item;
        }
    }

    @Override
    public Item findById(Long itemId) throws SQLException {
        if (itemId == null) return null;
        String sql = BASE_SELECT + "WHERE i.item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Item> findAll() throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE i.status != 'CLOSED' ORDER BY i.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    @Override
    public List<Item> findByType(String itemType) throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE i.item_type = ? AND i.status != 'CLOSED' ORDER BY i.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        }
        return items;
    }

    @Override
    public List<Item> findByUser(Long userId) throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE i.user_id = ? ORDER BY i.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        }
        return items;
    }

    @Override
    public List<Item> findActiveItems(String itemType) throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE i.item_type = ? AND i.status = 'ACTIVE' ORDER BY i.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        }
        return items;
    }

    @Override
    public boolean update(Item item) throws SQLException {
        String sql = "UPDATE items SET category_id = ?, title = ?, description = ?, " +
                     "location = ?, latitude = ?, longitude = ?, item_date = ?, image = ?, status = ? " +
                     "WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getCategoryId());
            ps.setString(2, item.getTitle());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getLocation());
            ps.setDouble(5, item.getLatitude() != null ? item.getLatitude() : 0.0);
            ps.setDouble(6, item.getLongitude() != null ? item.getLongitude() : 0.0);
            ps.setDate(7, item.getItemDate());
            ps.setString(8, item.getImage());
            ps.setString(9, item.getStatus());
            ps.setLong(10, item.getItemId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Long itemId) throws SQLException {
        // Soft delete: sets status to CLOSED
        return updateStatus(itemId, "CLOSED");
    }

    @Override
    public boolean updateStatus(Long itemId, String newStatus) throws SQLException {
        String sql = "UPDATE items SET status = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setLong(2, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, name, description FROM categories ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
        }
        return categories;
    }

    @Override
    public Category findCategoryById(Integer categoryId) throws SQLException {
        if (categoryId == null) return null;
        String sql = "SELECT category_id, name, description FROM categories WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getInt("category_id"),
                            rs.getString("name"),
                            rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public int countItemsByType(String itemType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM items WHERE item_type = ? AND status != 'CLOSED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public int countItemsByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM items WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
