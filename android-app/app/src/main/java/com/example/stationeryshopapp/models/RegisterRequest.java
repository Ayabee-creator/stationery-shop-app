package com.example.stationeryshopapp.models;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String phone;
    private String address;

    public RegisterRequest(String username, String email, String password, String phone, String address) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
    }
}