package com.example.stationeryshopapp.api;

import com.example.stationeryshopapp.models.CartResponse;
import com.example.stationeryshopapp.models.Category;
import com.example.stationeryshopapp.models.LoginRequest;
import com.example.stationeryshopapp.models.Order;
import com.example.stationeryshopapp.models.OrderDetailResponse;
import com.example.stationeryshopapp.models.Product;
import com.example.stationeryshopapp.models.RegisterRequest;
import com.example.stationeryshopapp.models.Review;
import com.example.stationeryshopapp.models.UserResponse;
import com.example.stationeryshopapp.models.WishlistItem;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ===== Products =====
    @GET("products")
    Call<List<Product>> getProducts();

    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") int productId);

    @GET("products/search")
    Call<List<Product>> searchProducts(@Query("name") String name);

    @GET("products/category/{categoryId}")
    Call<List<Product>> getProductsByCategory(@Path("categoryId") int categoryId);

    @GET("products/popular")
    Call<List<Product>> getPopularProducts();

    // ===== Categories =====
    @GET("categories")
    Call<List<Category>> getCategories();

    // ===== Users =====
    @POST("users/login")
    Call<UserResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("users/register")
    Call<UserResponse> registerUser(@Body RegisterRequest registerRequest);

    @GET("users/{id}")
    Call<Map<String, Object>> getUserProfile(@Path("id") int userId);

    @PUT("users/{id}")
    Call<Map<String, Object>> updateUserProfile(@Path("id") int userId, @Body Map<String, String> body);

    // ===== Orders =====
    @POST("orders")
    Call<Map<String, Object>> placeOrder(@Body Map<String, Object> body);

    @GET("orders/user/{userId}")
    Call<List<Order>> getOrdersByUser(@Path("userId") int userId);

    @GET("orders/{orderId}")
    Call<OrderDetailResponse> getOrderDetails(@Path("orderId") int orderId);

    // ===== Reviews =====
    @GET("reviews/product/{productId}")
    Call<List<Review>> getReviewsByProduct(@Path("productId") int productId);

    @POST("reviews")
    Call<Map<String, Object>> submitReview(@Body Map<String, Object> body);

    // ===== Cart =====
    @POST("cart")
    Call<Map<String, Object>> addToCart(@Body Map<String, Object> body);

    @GET("cart/user/{userId}")
    Call<CartResponse> getCart(@Path("userId") int userId);

    @PUT("cart/{id}")
    Call<Map<String, Object>> updateCartItem(@Path("id") int cartId, @Body Map<String, Object> body);

    @DELETE("cart/{id}")
    Call<Map<String, Object>> removeCartItem(@Path("id") int cartId);

    @DELETE("cart/user/{userId}")
    Call<Map<String, Object>> clearCart(@Path("userId") int userId);

    // ===== Wishlist =====
    @POST("wishlists")
    Call<Map<String, Object>> addToWishlist(@Body Map<String, Object> body);

    @GET("wishlists/user/{userId}")
    Call<List<WishlistItem>> getWishlist(@Path("userId") int userId);

    @DELETE("wishlists/{id}")
    Call<Map<String, Object>> removeFromWishlist(@Path("id") int wishlistId);

    @DELETE("wishlists/user/{userId}/product/{productId}")
    Call<Map<String, Object>> removeFromWishlistByProduct(@Path("userId") int userId, @Path("productId") int productId);
}
