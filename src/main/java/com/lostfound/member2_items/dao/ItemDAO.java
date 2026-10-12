package com.lostfound.member2_items.dao;

import com.lostfound.model.Category;
import com.lostfound.model.Item;

import java.sql.SQLException;
import java.util.List;

/**
 * Item Data Access Object Interface.
 * Handles database operations for Item and Category entities.
 */
public interface ItemDAO {

    /**
     * Creates a new item record.
     * @return Item with generated itemId.
     */
    Item create(Item item) throws SQLException;

    /**
     * Finds an item by primary key.
     */
    Item findById(Long itemId) throws SQLException;

    /**
     * Retrieves all items.
     */
    List<Item> findAll() throws SQLException;

    /**
     * Retrieves items filtered by type (LOST or FOUND).
     */
    List<Item> findByType(String itemType) throws SQLException;

    /**
     * Retrieves items reported by a specific user.
     */
    List<Item> findByUser(Long userId) throws SQLException;

    /**
     * Retrieves active items of a specific type (e.g. for matching or gallery).
     */
    List<Item> findActiveItems(String itemType) throws SQLException;

    /**
     * Updates item details.
     */
    boolean update(Item item) throws SQLException;

    /**
     * Soft deletes / closes an item.
     */
    boolean delete(Long itemId) throws SQLException;

    /**
     * Updates item status (ACTIVE, MATCHED, CLAIMED, RETURNED, CLOSED).
     */
    boolean updateStatus(Long itemId, String newStatus) throws SQLException;

    /**
     * Retrieves all available categories.
     */
    List<Category> getAllCategories() throws SQLException;

    /**
     * Finds category by categoryId.
     */
    Category findCategoryById(Integer categoryId) throws SQLException;

    /**
     * Counts items by type (LOST / FOUND).
     */
    int countItemsByType(String itemType) throws SQLException;

    /**
     * Counts items by status.
     */
    int countItemsByStatus(String status) throws SQLException;
}
