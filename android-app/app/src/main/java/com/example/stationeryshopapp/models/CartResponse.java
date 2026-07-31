package com.example.stationeryshopapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CartResponse {

    private List<CartItem> items;

    @SerializedName("item_count")
    private int itemCount;

    @SerializedName("total_amount")
    private double totalAmount;

    public List<CartItem> getItems() { return items; }
    public int getItemCount() { return itemCount; }
    public double getTotalAmount() { return totalAmount; }
}
