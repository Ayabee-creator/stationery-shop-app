package com.example.stationeryshopapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderDetailResponse {

    private int id;

    @SerializedName("user_id")
    private int userId;

    private String username;

    @SerializedName("total_amount")
    private double totalAmount;

    private String status;

    @SerializedName("ordered_at")
    private String orderedAt;

    private List<OrderItem> items;

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getOrderedAt() { return orderedAt; }
    public List<OrderItem> getItems() { return items; }
}
