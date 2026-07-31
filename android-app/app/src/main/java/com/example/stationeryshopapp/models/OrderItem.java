package com.example.stationeryshopapp.models;

import com.google.gson.annotations.SerializedName;

public class OrderItem {

    @SerializedName("product_id")
    private int productId;

    @SerializedName("product_name")
    private String productName;

    private int quantity;

    @SerializedName("unit_price")
    private double unitPrice;

    @SerializedName("line_total")
    private double lineTotal;

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getLineTotal() { return lineTotal; }
}
