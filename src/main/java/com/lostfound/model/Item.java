package com.lostfound.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Item Entity POJO.
 */
public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private Long userId;
    private Integer categoryId;
    private String title;
    private String description;
    private String itemType; // LOST, FOUND
    private String location;
    private Double latitude;
    private Double longitude;
    private Date itemDate;
    private String image;
    private String status; // ACTIVE, MATCHED, CLAIMED, RETURNED, CLOSED
    private Timestamp createdAt;

    // Joined auxiliary fields for UI presentation
    private String userName;
    private String userEmail;
    private String userPhone;
    private String categoryName;

    public Item() {
    }

    public Item(Long itemId, Long userId, Integer categoryId, String title, String description,
                String itemType, String location, Double latitude, Double longitude,
                Date itemDate, String image, String status, Timestamp createdAt) {
        this.itemId = itemId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.itemType = itemType;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.itemDate = itemDate;
        this.image = image;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Date getItemDate() {
        return itemDate;
    }

    public void setItemDate(Date itemDate) {
        this.itemDate = itemDate;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId=" + itemId +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", title='" + title + '\'' +
                ", itemType='" + itemType + '\'' +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
