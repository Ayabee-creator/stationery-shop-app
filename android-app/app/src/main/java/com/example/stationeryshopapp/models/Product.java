package com.example.stationeryshopapp.models;

import com.google.gson.annotations.SerializedName;

public class Product {

    private int id;
    private String name;
    private String category;

    @SerializedName("category_id")
    private int categoryId;

    private String description;
    private double price;

    @SerializedName("stock_quantity")
    private int stockQuantity;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("avg_rating")
    private double avgRating;

    @SerializedName("review_count")
    private int reviewCount;

    @SerializedName("total_ordered")
    private int totalOrdered;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public int getTotalOrdered() {
        return totalOrdered;
    }
}
