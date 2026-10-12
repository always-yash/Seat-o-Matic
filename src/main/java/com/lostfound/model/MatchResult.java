package com.lostfound.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * MatchResult Entity POJO.
 * Stores algorithm matching scores between LOST and FOUND items.
 */
public class MatchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long matchId;
    private Long lostItemId;
    private Long foundItemId;
    private double textScore;
    private double locationScore;
    private double dateScore;
    private double categoryScore;
    private double totalScore;
    private Timestamp createdAt;

    // Associated item details
    private Item lostItem;
    private Item foundItem;

    public MatchResult() {
    }

    public MatchResult(Long matchId, Long lostItemId, Long foundItemId, double textScore,
                       double locationScore, double dateScore, double categoryScore,
                       double totalScore, Timestamp createdAt) {
        this.matchId = matchId;
        this.lostItemId = lostItemId;
        this.foundItemId = foundItemId;
        this.textScore = textScore;
        this.locationScore = locationScore;
        this.dateScore = dateScore;
        this.categoryScore = categoryScore;
        this.totalScore = totalScore;
        this.createdAt = createdAt;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getLostItemId() {
        return lostItemId;
    }

    public void setLostItemId(Long lostItemId) {
        this.lostItemId = lostItemId;
    }

    public Long getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(Long foundItemId) {
        this.foundItemId = foundItemId;
    }

    public double getTextScore() {
        return textScore;
    }

    public void setTextScore(double textScore) {
        this.textScore = textScore;
    }

    public double getLocationScore() {
        return locationScore;
    }

    public void setLocationScore(double locationScore) {
        this.locationScore = locationScore;
    }

    public double getDateScore() {
        return dateScore;
    }

    public void setDateScore(double dateScore) {
        this.dateScore = dateScore;
    }

    public double getCategoryScore() {
        return categoryScore;
    }

    public void setCategoryScore(double categoryScore) {
        this.categoryScore = categoryScore;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Item getLostItem() {
        return lostItem;
    }

    public void setLostItem(Item lostItem) {
        this.lostItem = lostItem;
    }

    public Item getFoundItem() {
        return foundItem;
    }

    public void setFoundItem(Item foundItem) {
        this.foundItem = foundItem;
    }

    public String getRating() {
        if (totalScore >= 85.0) {
            return "Excellent Match";
        } else if (totalScore >= 70.0) {
            return "Good Match";
        } else if (totalScore >= 50.0) {
            return "Possible Match";
        } else {
            return "Low Match";
        }
    }

    public String getRatingBadgeClass() {
        if (totalScore >= 85.0) {
            return "bg-success";
        } else if (totalScore >= 70.0) {
            return "bg-primary";
        } else if (totalScore >= 50.0) {
            return "bg-warning text-dark";
        } else {
            return "bg-secondary";
        }
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "matchId=" + matchId +
                ", lostItemId=" + lostItemId +
                ", foundItemId=" + foundItemId +
                ", totalScore=" + totalScore +
                ", rating='" + getRating() + '\'' +
                '}';
    }
}
