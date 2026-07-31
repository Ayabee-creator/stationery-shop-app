package com.example.stationeryshopapp.models;

import com.google.gson.annotations.SerializedName;

public class Order {

    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("total_amount")
    private double totalAmount;

    private String status;

    @SerializedName("ordered_at")
    private String orderedAt;

    @SerializedName("item_count")
    private int itemCount;

    private String username;

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getOrderedAt() { return orderedAt; }
    public int getItemCount() { return itemCount; }
    public String getUsername() { return username; }
}
