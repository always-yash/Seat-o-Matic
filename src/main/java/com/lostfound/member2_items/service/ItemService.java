package com.lostfound.member2_items.service;

import com.lostfound.model.Category;
import com.lostfound.model.Item;

import java.util.List;

/**
 * Item Service Interface.
 * Handles business logic, ownership authorization, and reporting workflows.
 */
public interface ItemService {

    Item reportLost(Item item) throws Exception;

    Item reportFound(Item item) throws Exception;

    Item getItemById(Long itemId) throws Exception;

    List<Item> getAllItems() throws Exception;

    List<Item> getItemsByType(String type) throws Exception;

    List<Item> getItemsByUser(Long userId) throws Exception;

    boolean updateItem(Item item, Long requestingUserId, String requestingRole) throws Exception;

    boolean deleteItem(Long itemId, Long requestingUserId, String requestingRole) throws Exception;

    boolean updateStatus(Long itemId, String status) throws Exception;

    List<Category> getAllCategories() throws Exception;
}
