package com.example.stationeryshopapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stationeryshopapp.api.ApiService;
import com.example.stationeryshopapp.api.RetrofitClient;
import com.example.stationeryshopapp.models.WishlistItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView recyclerWishlist;
    private LinearLayout emptyState;
    private ImageView btnBack;
    private WishlistAdapter wishlistAdapter;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        recyclerWishlist = findViewById(R.id.recyclerWishlist);
        emptyState = findViewById(R.id.emptyState);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        userId = prefs.getInt("user_id", 0);

        wishlistAdapter = new WishlistAdapter();
        recyclerWishlist.setLayoutManager(new LinearLayoutManager(this));
        recyclerWishlist.setAdapter(wishlistAdapter);

        loadWishlist();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWishlist();
    }

    private void loadWishlist() {
        if (userId == 0) {
            emptyState.setVisibility(View.VISIBLE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getWishlist(userId).enqueue(new Callback<List<WishlistItem>>() {
            @Override
            public void onResponse(Call<List<WishlistItem>> call, Response<List<WishlistItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<WishlistItem> items = response.body();
                    if (items.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        recyclerWishlist.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        recyclerWishlist.setVisibility(View.VISIBLE);
                        wishlistAdapter.setItems(items);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<WishlistItem>> call, Throwable t) {
                Toast.makeText(WishlistActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromWishlist(int wishlistId) {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.removeFromWishlist(wishlistId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Toast.makeText(WishlistActivity.this, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                loadWishlist();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(WishlistActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart(WishlistItem item) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("product_id", item.getProductId());
        body.put("quantity", 1);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.addToCart(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(WishlistActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WishlistActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(WishlistActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Wishlist Adapter
    private class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistVH> {

        private List<WishlistItem> items = new ArrayList<>();

        void setItems(List<WishlistItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public WishlistVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart, parent, false);
            return new WishlistVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WishlistVH holder, int position) {
            WishlistItem item = items.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() { return items.size(); }

        class WishlistVH extends RecyclerView.ViewHolder {
            TextView txtItemName, txtItemPrice, txtQuantity, btnMinus, btnPlus;
            ImageView btnRemove, imgCartProduct;

            WishlistVH(@NonNull View itemView) {
                super(itemView);
                txtItemName = itemView.findViewById(R.id.txtItemName);
                txtItemPrice = itemView.findViewById(R.id.txtItemPrice);
                txtQuantity = itemView.findViewById(R.id.txtQuantity);
                btnMinus = itemView.findViewById(R.id.btnMinus);
                btnPlus = itemView.findViewById(R.id.btnPlus);
                btnRemove = itemView.findViewById(R.id.btnRemove);
                imgCartProduct = itemView.findViewById(R.id.imgCartProduct);
            }

            void bind(WishlistItem item) {
                txtItemName.setText(item.getName());
                txtItemPrice.setText(String.format("R%.2f", item.getPrice()));
                txtQuantity.setText(item.getCategory());

                // Load image with Glide
                if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                    com.bumptech.glide.Glide.with(itemView.getContext())
                            .load(item.getImageUrl())
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .into(imgCartProduct);
                }

                // Repurpose minus button as "Add to Cart"
                btnMinus.setText("🛒");
                btnMinus.setOnClickListener(v -> addToCart(item));

                // Hide plus button
                btnPlus.setVisibility(View.GONE);

                btnRemove.setOnClickListener(v -> removeFromWishlist(item.getId()));

                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(WishlistActivity.this, ProductDetailActivity.class);
                    intent.putExtra("product_id", item.getProductId());
                    startActivity(intent);
                });
            }
        }
    }
}
