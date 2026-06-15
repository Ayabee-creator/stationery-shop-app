package com.example.stationeryshopapp.api;

import com.example.stationeryshopapp.models.LoginRequest;
import com.example.stationeryshopapp.models.Product;
import com.example.stationeryshopapp.models.RegisterRequest;
import com.example.stationeryshopapp.models.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @GET("products")
    Call<List<Product>> getProducts();

    @POST("users/login")
    Call<UserResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("users/register")
    Call<UserResponse> registerUser(@Body RegisterRequest registerRequest);
}