package com.lostfound.member3_matching.service;

import com.lostfound.member3_matching.dao.SearchDAOImpl;
import com.lostfound.model.Item;

import java.sql.Date;
import java.util.List;

/**
 * Search Service.
 * Coordinates multi-criteria search queries across items.
 */
public class SearchService {

    private final SearchDAOImpl searchDAO = new SearchDAOImpl();

    public List<Item> search(String keyword, Integer categoryId, String location,
                             String itemType, Date itemDate, String status) throws Exception {
        return searchDAO.searchItems(keyword, categoryId, location, itemType, itemDate, status);
    }
}
