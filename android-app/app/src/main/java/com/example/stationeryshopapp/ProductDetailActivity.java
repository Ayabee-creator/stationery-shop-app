package com.example.stationeryshopapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stationeryshopapp.api.ApiService;
import com.example.stationeryshopapp.api.RetrofitClient;
import com.example.stationeryshopapp.models.Product;
import com.example.stationeryshopapp.models.Review;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView txtProductName, txtCategory, txtDescription, txtPrice, txtStock;
    private TextView txtRating, txtStars, txtReviewCount;
    private MaterialButton btnAddToCart, btnWriteReview;
    private ImageView btnBack, btnWishlist;
    private RecyclerView recyclerReviews;

    private int productId;
    private Product currentProduct;
    private boolean isInWishlist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        txtProductName = findViewById(R.id.txtProductName);
        txtCategory = findViewById(R.id.txtCategory);
        txtDescription = findViewById(R.id.txtDescription);
        txtPrice = findViewById(R.id.txtPrice);
        txtStock = findViewById(R.id.txtStock);
        txtRating = findViewById(R.id.txtRating);
        txtStars = findViewById(R.id.txtStars);
        txtReviewCount = findViewById(R.id.txtReviewCount);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        btnBack = findViewById(R.id.btnBack);
        btnWishlist = findViewById(R.id.btnWishlist);
        recyclerReviews = findViewById(R.id.recyclerReviews);

        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        productId = getIntent().getIntExtra("product_id", 0);
        if (productId > 0) {
            loadProduct();
            loadReviews();
        }

        btnAddToCart.setOnClickListener(v -> addToCart());
        btnWriteReview.setOnClickListener(v -> showReviewDialog());
        btnWishlist.setOnClickListener(v -> toggleWishlist());
    }

    private void loadProduct() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    displayProduct(currentProduct);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Error loading product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProduct(Product product) {
        txtProductName.setText(product.getName());
        txtCategory.setText(product.getCategory());
        txtPrice.setText(String.format("R%.2f", product.getPrice()));
        txtStock.setText(product.getStockQuantity() + " in stock");

        // Load image with Glide
        ImageView imgProduct = findViewById(R.id.imgProduct);
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(product.getImageUrl())
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(imgProduct);
        }

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            txtDescription.setText(product.getDescription());
        } else {
            txtDescription.setVisibility(View.GONE);
        }

        // Rating
        double rating = product.getAvgRating();
        txtRating.setText(String.format("%.1f", rating));
        txtStars.setText(getStarString(rating));
        txtReviewCount.setText("(" + product.getReviewCount() + " reviews)");
    }

    private void loadReviews() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getReviewsByProduct(productId).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recyclerReviews.setAdapter(new ReviewAdapter(response.body()));
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                // Silent fail for reviews
            }
        });
    }

    private void addToCart() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        if (userId == 0) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("product_id", productId);
        body.put("quantity", 1);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.addToCart(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleWishlist() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        if (userId == 0) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isInWishlist) {
            ApiService apiService = RetrofitClient.getApiService();
            apiService.removeFromWishlistByProduct(userId, productId).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    isInWishlist = false;
                    btnWishlist.setImageResource(android.R.drawable.btn_star_big_off);
                    Toast.makeText(ProductDetailActivity.this, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Map<String, Object> body = new HashMap<>();
            body.put("user_id", userId);
            body.put("product_id", productId);

            ApiService apiService = RetrofitClient.getApiService();
            apiService.addToWishlist(body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        isInWishlist = true;
                        btnWishlist.setImageResource(android.R.drawable.btn_star_big_on);
                        Toast.makeText(ProductDetailActivity.this, "Added to wishlist!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Already in wishlist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showReviewDialog() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        if (userId == 0) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null);

        // Create a simple dialog with rating input
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Write a Review");

        final EditText input = new EditText(this);
        input.setHint("Enter your review (1-5 stars in first char, then comment)");
        input.setHint("Rating (1-5), then your comment...");
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) return;

            // Parse: first char is rating, rest is comment
            int rating = 5;
            String comment = text;
            if (text.length() > 1 && Character.isDigit(text.charAt(0))) {
                rating = Character.getNumericValue(text.charAt(0));
                if (rating < 1) rating = 1;
                if (rating > 5) rating = 5;
                comment = text.substring(1).trim();
                if (comment.startsWith(",") || comment.startsWith(".") || comment.startsWith("-")) {
                    comment = comment.substring(1).trim();
                }
            }

            submitReview(userId, rating, comment);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void submitReview(int userId, int rating, String comment) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("product_id", productId);
        body.put("rating", rating);
        body.put("comment", comment);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.submitReview(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, "Review submitted!", Toast.LENGTH_SHORT).show();
                    loadReviews(); // Refresh reviews
                    loadProduct(); // Refresh rating
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStarString(double rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) stars.append("★");
            else stars.append("☆");
        }
        return stars.toString();
    }

    // Review adapter
    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewVH> {

        private final List<Review> reviews;

        ReviewAdapter(List<Review> reviews) {
            this.reviews = reviews != null ? reviews : new ArrayList<>();
        }

        @NonNull
        @Override
        public ReviewVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review, parent, false);
            return new ReviewVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewVH holder, int position) {
            Review review = reviews.get(position);
            holder.txtReviewUser.setText(review.getUsername());
            holder.txtReviewStars.setText(getStarString(review.getRating()));
            holder.txtReviewComment.setText(review.getComment());
            holder.txtReviewDate.setText(formatDate(review.getCreatedAt()));
        }

        @Override
        public int getItemCount() { return reviews.size(); }

        class ReviewVH extends RecyclerView.ViewHolder {
            TextView txtReviewUser, txtReviewStars, txtReviewComment, txtReviewDate;

            ReviewVH(@NonNull View itemView) {
                super(itemView);
                txtReviewUser = itemView.findViewById(R.id.txtReviewUser);
                txtReviewStars = itemView.findViewById(R.id.txtReviewStars);
                txtReviewComment = itemView.findViewById(R.id.txtReviewComment);
                txtReviewDate = itemView.findViewById(R.id.txtReviewDate);
            }
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null) return "";
        if (dateStr.length() >= 10) return dateStr.substring(0, 10);
        return dateStr;
    }
}
