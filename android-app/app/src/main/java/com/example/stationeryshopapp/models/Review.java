package com.example.stationeryshopapp.models;

import com.google.gson.annotations.SerializedName;

public class Review {

    private int id;

    @SerializedName("user_id")
    private int userId;

    private String username;

    @SerializedName("product_id")
    private int productId;

    @SerializedName("product_name")
    private String productName;

    private int rating;
    private String comment;

    @SerializedName("created_at")
    private String createdAt;

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
}
