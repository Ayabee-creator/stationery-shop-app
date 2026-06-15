package com.example.stationeryshopapp.models;

import com.google.gson.annotations.SerializedName;

public class Product {

    private int id;
    private String name;
    private String category;
    private double price;

    @SerializedName("stock_quantity")
    private int stockQuantity;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }
}