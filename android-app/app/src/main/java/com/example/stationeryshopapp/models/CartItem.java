package com.example.stationeryshopapp.models;

import com.google.gson.annotations.SerializedName;

public class CartItem {

    private int id;

    @SerializedName("product_id")
    private int productId;

    private String name;
    private String description;
    private double price;
    private int quantity;

    @SerializedName("stock_quantity")
    private int stockQuantity;

    @SerializedName("image_url")
    private String imageUrl;

    private String category;

    @SerializedName("line_total")
    private double lineTotal;

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getStockQuantity() { return stockQuantity; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public double getLineTotal() { return lineTotal; }
}
