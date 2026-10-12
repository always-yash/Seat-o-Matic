package com.lostfound.member2_items.service;

import com.lostfound.member1_auth.util.ValidationUtil;
import com.lostfound.member2_items.dao.ItemDAO;
import com.lostfound.member2_items.dao.ItemDAOImpl;
import com.lostfound.model.Category;
import com.lostfound.model.Item;

import java.util.List;

/**
 * Implementation of ItemService with validation and authorization rules.
 */
public class ItemServiceImpl implements ItemService {

    private final ItemDAO itemDAO;

    public ItemServiceImpl() {
        this.itemDAO = new ItemDAOImpl();
    }

    public ItemServiceImpl(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    private void validateItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item details cannot be empty.");
        }
        if (!ValidationUtil.isNonEmpty(item.getTitle()) || item.getTitle().trim().length() < 3) {
            throw new IllegalArgumentException("Title is required and must be at least 3 characters.");
        }
        if (!ValidationUtil.isNonEmpty(item.getDescription()) || item.getDescription().trim().length() < 5) {
            throw new IllegalArgumentException("Description is required and must be at least 5 characters.");
        }
        if (item.getCategoryId() == null || item.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Please select a valid item category.");
        }
        if (!ValidationUtil.isNonEmpty(item.getLocation())) {
            throw new IllegalArgumentException("Location where the item was lost/found is required.");
        }
        if (item.getItemDate() == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        if (item.getUserId() == null) {
            throw new IllegalArgumentException("User association is required.");
        }
    }

    @Override
    public Item reportLost(Item item) throws Exception {
        validateItem(item);
        item.setItemType("LOST");
        item.setStatus("ACTIVE");
        return itemDAO.create(item);
    }

    @Override
    public Item reportFound(Item item) throws Exception {
        validateItem(item);
        item.setItemType("FOUND");
        item.setStatus("ACTIVE");
        return itemDAO.create(item);
    }

    @Override
    public Item getItemById(Long itemId) throws Exception {
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID is required.");
        }
        Item item = itemDAO.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found with ID: " + itemId);
        }
        return item;
    }

    @Override
    public List<Item> getAllItems() throws Exception {
        return itemDAO.findAll();
    }

    @Override
    public List<Item> getItemsByType(String type) throws Exception {
        return itemDAO.findByType(type);
    }

    @Override
    public List<Item> getItemsByUser(Long userId) throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }
        return itemDAO.findByUser(userId);
    }

    @Override
    public boolean updateItem(Item item, Long requestingUserId, String requestingRole) throws Exception {
        validateItem(item);
        if (item.getItemId() == null) {
            throw new IllegalArgumentException("Item ID is required for update.");
        }

        Item existing = itemDAO.findById(item.getItemId());
        if (existing == null) {
            throw new IllegalArgumentException("Item not found.");
        }

        // Strict authorization check: Only owner or ADMIN can update
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requestingRole);
        boolean isOwner = requestingUserId != null && requestingUserId.equals(existing.getUserId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Forbidden: You do not have permission to modify this item.");
        }

        // Retain original type and creation date
        item.setItemType(existing.getItemType());
        if (item.getStatus() == null) {
            item.setStatus(existing.getStatus());
        }

        return itemDAO.update(item);
    }

    @Override
    public boolean deleteItem(Long itemId, Long requestingUserId, String requestingRole) throws Exception {
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID is required.");
        }

        Item existing = itemDAO.findById(itemId);
        if (existing == null) {
            throw new IllegalArgumentException("Item not found.");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(requestingRole);
        boolean isOwner = requestingUserId != null && requestingUserId.equals(existing.getUserId());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Forbidden: You do not have permission to delete this item.");
        }

        return itemDAO.delete(itemId);
    }

    @Override
    public boolean updateStatus(Long itemId, String status) throws Exception {
        if (itemId == null || !ValidationUtil.isNonEmpty(status)) {
            throw new IllegalArgumentException("Item ID and new status are required.");
        }
        return itemDAO.updateStatus(itemId, status);
    }

    @Override
    public List<Category> getAllCategories() throws Exception {
        return itemDAO.getAllCategories();
    }
}
