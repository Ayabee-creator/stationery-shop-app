package com.example.stationeryshopapp.models;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    private boolean success;
    private String message;

    @SerializedName("user_id")
    private int userId;

    private String username;
    private String email;
    private String phone;
    private String address;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}