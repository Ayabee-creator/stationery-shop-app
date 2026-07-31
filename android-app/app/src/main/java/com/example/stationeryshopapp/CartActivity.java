package com.example.stationeryshopapp;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stationeryshopapp.api.ApiService;
import com.example.stationeryshopapp.api.RetrofitClient;
import com.example.stationeryshopapp.models.CartItem;
import com.example.stationeryshopapp.models.CartResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerCart;
    private LinearLayout emptyState, checkoutBar;
    private TextView txtTotal, txtClearCart;
    private MaterialButton btnCheckout;
    private ImageView btnBack;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerCart = findViewById(R.id.recyclerCart);
        emptyState = findViewById(R.id.emptyState);
        checkoutBar = findViewById(R.id.checkoutBar);
        txtTotal = findViewById(R.id.txtTotal);
        txtClearCart = findViewById(R.id.txtClearCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        userId = prefs.getInt("user_id", 0);

        cartAdapter = new CartAdapter();
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(cartAdapter);

        txtClearCart.setOnClickListener(v -> clearCart());
        btnCheckout.setOnClickListener(v -> placeOrder());

        loadCart();
    }

    private void loadCart() {
        if (userId == 0) {
            showEmpty();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getCart(userId).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CartResponse cart = response.body();
                    cartItems = cart.getItems();

                    if (cartItems == null || cartItems.isEmpty()) {
                        showEmpty();
                    } else {
                        emptyState.setVisibility(View.GONE);
                        recyclerCart.setVisibility(View.VISIBLE);
                        checkoutBar.setVisibility(View.VISIBLE);
                        cartAdapter.setItems(cartItems);
                        txtTotal.setText(String.format("R%.2f", cart.getTotalAmount()));
                    }
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmpty() {
        emptyState.setVisibility(View.VISIBLE);
        recyclerCart.setVisibility(View.GONE);
        checkoutBar.setVisibility(View.GONE);
    }

    private void clearCart() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Cart")
                .setMessage("Remove all items from your cart?")
                .setPositiveButton("Clear", (d, w) -> {
                    ApiService apiService = RetrofitClient.getApiService();
                    apiService.clearCart(userId).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            Toast.makeText(CartActivity.this, "Cart cleared", Toast.LENGTH_SHORT).show();
                            showEmpty();
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(CartActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void placeOrder() {
        if (cartItems == null || cartItems.isEmpty()) return;

        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, Object> orderItem = new HashMap<>();
            orderItem.put("product_id", item.getProductId());
            orderItem.put("quantity", item.getQuantity());
            items.add(orderItem);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("items", items);

        btnCheckout.setEnabled(false);
        btnCheckout.setText("Processing...");

        ApiService apiService = RetrofitClient.getApiService();
        apiService.placeOrder(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Place Order");

                if (response.isSuccessful()) {
                    // Clear cart after successful order
                    apiService.clearCart(userId).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {}
                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                    });

                    new AlertDialog.Builder(CartActivity.this)
                            .setTitle("Order Placed!")
                            .setMessage("Your order has been placed successfully.")
                            .setPositiveButton("OK", (d, w) -> finish())
                            .setCancelable(false)
                            .show();
                } else {
                    Toast.makeText(CartActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Place Order");
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartItemQuantity(int cartId, int newQuantity) {
        Map<String, Object> body = new HashMap<>();
        body.put("quantity", newQuantity);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.updateCartItem(cartId, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                loadCart(); // Refresh
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error updating cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeCartItem(int cartId) {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.removeCartItem(cartId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Toast.makeText(CartActivity.this, "Removed from cart", Toast.LENGTH_SHORT).show();
                loadCart();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cart Adapter
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartVH> {

        private List<CartItem> items = new ArrayList<>();

        void setItems(List<CartItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CartVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart, parent, false);
            return new CartVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartVH holder, int position) {
            CartItem item = items.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() { return items.size(); }

        class CartVH extends RecyclerView.ViewHolder {
            TextView txtItemName, txtItemPrice, txtQuantity, btnMinus, btnPlus;
            ImageView btnRemove, imgCartProduct;

            CartVH(@NonNull View itemView) {
                super(itemView);
                txtItemName = itemView.findViewById(R.id.txtItemName);
                txtItemPrice = itemView.findViewById(R.id.txtItemPrice);
                txtQuantity = itemView.findViewById(R.id.txtQuantity);
                btnMinus = itemView.findViewById(R.id.btnMinus);
                btnPlus = itemView.findViewById(R.id.btnPlus);
                btnRemove = itemView.findViewById(R.id.btnRemove);
                imgCartProduct = itemView.findViewById(R.id.imgCartProduct);
            }

            void bind(CartItem item) {
                txtItemName.setText(item.getName());
                txtItemPrice.setText(String.format("R%.2f", item.getPrice()));
                txtQuantity.setText(String.valueOf(item.getQuantity()));

                // Load image with Glide
                if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                    com.bumptech.glide.Glide.with(itemView.getContext())
                            .load(item.getImageUrl())
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .into(imgCartProduct);
                }

                btnPlus.setOnClickListener(v -> {
                    int newQty = item.getQuantity() + 1;
                    updateCartItemQuantity(item.getId(), newQty);
                });

                btnMinus.setOnClickListener(v -> {
                    int newQty = item.getQuantity() - 1;
                    if (newQty <= 0) {
                        removeCartItem(item.getId());
                    } else {
                        updateCartItemQuantity(item.getId(), newQty);
                    }
                });

                btnRemove.setOnClickListener(v -> removeCartItem(item.getId()));
            }
        }
    }
}
