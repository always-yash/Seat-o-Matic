package com.lostfound.member3_matching.service;

import com.lostfound.member2_items.dao.ItemDAO;
import com.lostfound.member2_items.dao.ItemDAOImpl;
import com.lostfound.member3_matching.algo.HaversineAlgorithm;
import com.lostfound.member3_matching.algo.LevenshteinAlgorithm;
import com.lostfound.member3_matching.dao.SearchDAOImpl;
import com.lostfound.model.Item;
import com.lostfound.model.MatchResult;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;



/**
 * Match Service.
 * Implements the automated matching engine combining Levenshtein,
 * Haversine, Temporal proximity, and Category correlation.
 */
public class MatchService {

    private final ItemDAO itemDAO = new ItemDAOImpl();
    private final SearchDAOImpl searchDAO = new SearchDAOImpl();

    /**
     * Calculates matches for a specific item against the opposite item pool.
     * (If item is LOST -> checks against FOUND; if item is FOUND -> checks against LOST)
     */
    public List<MatchResult> findMatchesForItem(Long itemId) throws Exception {
        Item target = itemDAO.findById(itemId);
        if (target == null) {
            throw new IllegalArgumentException("Target item not found with ID: " + itemId);
        }

        String oppositeType = "LOST".equalsIgnoreCase(target.getItemType()) ? "FOUND" : "LOST";
        List<Item> candidates = itemDAO.findActiveItems(oppositeType);

        List<MatchResult> results = new ArrayList<>();

        for (Item candidate : candidates) {
            // Cannot match with an item reported by the exact same user
            if (target.getUserId().equals(candidate.getUserId())) {
                continue;
            }

            MatchResult match = calculateMatchScores(target, candidate);

            // Keep matches that reach a baseline similarity
            if (match.getTotalScore() >= 35.0) {
                // Persist score in database
                try {
                    Long matchId = searchDAO.saveMatch(match);
                    match.setMatchId(matchId);
                } catch (Exception ignored) {
                }
                results.add(match);
            }
        }

        // Sort descending by total score
        results.sort(Comparator.comparingDouble(MatchResult::getTotalScore).reversed());

        return results;
    }

    /**
     * Calculates the 4-component score between two items.
     */
    public MatchResult calculateMatchScores(Item itemA, Item itemB) {
        Item lost = "LOST".equalsIgnoreCase(itemA.getItemType()) ? itemA : itemB;
        Item found = "FOUND".equalsIgnoreCase(itemA.getItemType()) ? itemA : itemB;

        // 1. Text Score (40%)
        String textA = itemA.getTitle() + " " + itemA.getDescription();
        String textB = itemB.getTitle() + " " + itemB.getDescription();
        double textScore = LevenshteinAlgorithm.calculateTextScore(textA, textB);

        // 2. Location Score (30%)
        double locationScore = HaversineAlgorithm.calculateLocationScore(
                itemA.getLatitude(), itemA.getLongitude(), itemA.getLocation(),
                itemB.getLatitude(), itemB.getLongitude(), itemB.getLocation()
        );

        // 3. Date Score (20%)
        double dateScore = calculateDateScore(itemA.getItemDate(), itemB.getItemDate());

        // 4. Category Score (10%)
        double categoryScore = (itemA.getCategoryId() != null && itemA.getCategoryId().equals(itemB.getCategoryId())) ? 100.0 : 0.0;

        // Formula: totalScore = (textScore * 0.40) + (locationScore * 0.30) + (dateScore * 0.20) + (categoryScore * 0.10)
        double totalScore = (textScore * 0.40) +
                            (locationScore * 0.30) +
                            (dateScore * 0.20) +
                            (categoryScore * 0.10);

        totalScore = Math.round(totalScore * 100.0) / 100.0;

        MatchResult match = new MatchResult();
        match.setLostItemId(lost.getItemId());
        match.setFoundItemId(found.getItemId());
        match.setLostItem(lost);
        match.setFoundItem(found);
        match.setTextScore(textScore);
        match.setLocationScore(locationScore);
        match.setDateScore(dateScore);
        match.setCategoryScore(categoryScore);
        match.setTotalScore(totalScore);
        match.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        return match;
    }

    private double calculateDateScore(Date d1, Date d2) {
        if (d1 == null || d2 == null) return 50.0;

        long daysDiff = Math.abs(ChronoUnit.DAYS.between(d1.toLocalDate(), d2.toLocalDate()));

        if (daysDiff == 0) {
            return 100.0;
        } else if (daysDiff <= 3) {
            return 85.0;
        } else if (daysDiff <= 7) {
            return 70.0;
        } else if (daysDiff <= 14) {
            return 50.0;
        } else if (daysDiff <= 30) {
            return 30.0;
        } else {
            return 15.0;
        }
    }
}
