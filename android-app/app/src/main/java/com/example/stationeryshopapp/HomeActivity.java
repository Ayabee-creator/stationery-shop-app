package com.example.stationeryshopapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stationeryshopapp.adapters.ProductAdapter;
import com.example.stationeryshopapp.api.ApiService;
import com.example.stationeryshopapp.api.RetrofitClient;
import com.example.stationeryshopapp.models.Product;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private TextView txtWelcome;
    private EditText edtSearch;
    private RecyclerView recyclerProducts;
    private ProductAdapter productAdapter;

    private FrameLayout btnCart, btnProfile;
    private MaterialButton btnProducts, btnExploreKits, btnBuildKit;
    private TextView txtSeeAll;

    // Bottom nav
    private LinearLayout navHome, navProducts, navOrders, navLogout;

    // Category chips
    private TextView chipAll, chipPens, chipNotebooks, chipArt, chipOffice, chipPaper;
    private TextView selectedChip;

    private List<Product> allProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Header
        txtWelcome = findViewById(R.id.txtWelcome);
        edtSearch = findViewById(R.id.edtSearch);
        btnCart = findViewById(R.id.btnCart);
        btnProfile = findViewById(R.id.btnProfile);

        // Hero buttons
        btnProducts = findViewById(R.id.btnProducts);
        btnExploreKits = findViewById(R.id.btnExploreKits);
        btnBuildKit = findViewById(R.id.btnBuildKit);
        txtSeeAll = findViewById(R.id.txtSeeAll);

        // RecyclerView
        recyclerProducts = findViewById(R.id.recyclerProducts);
        productAdapter = new ProductAdapter(this);
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setAdapter(productAdapter);

        // Category chips
        chipAll = findViewById(R.id.chipAll);
        chipPens = findViewById(R.id.chipPens);
        chipNotebooks = findViewById(R.id.chipNotebooks);
        chipArt = findViewById(R.id.chipArt);
        chipOffice = findViewById(R.id.chipOffice);
        chipPaper = findViewById(R.id.chipPaper);
        selectedChip = chipAll;
        setupChipClickListeners();

        // Bottom nav
        navHome = findViewById(R.id.navHome);
        navProducts = findViewById(R.id.navProducts);
        navOrders = findViewById(R.id.navOrders);
        navLogout = findViewById(R.id.navLogout);

        // Set welcome message
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "Student");
        txtWelcome.setText("Welcome, " + username);

        // Button listeners
        btnProducts.setOnClickListener(v -> openProductList());
        txtSeeAll.setOnClickListener(v -> openProductList());

        btnExploreKits.setOnClickListener(v -> {
            Toast.makeText(this, "Study Kits coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnBuildKit.setOnClickListener(v -> {
            Toast.makeText(this, "Kit Builder coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnCart.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CartActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        // Bottom nav listeners
        navHome.setOnClickListener(v -> {
            // Already on home
        });

        navProducts.setOnClickListener(v -> openProductList());

        navOrders.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, OrderHistoryActivity.class));
        });

        navLogout.setOnClickListener(v -> logoutUser());

        // Search functionality
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProductsBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load products
        loadFeaturedProducts();
    }

    private void setupChipClickListeners() {
        chipAll.setOnClickListener(v -> selectCategory(chipAll, 0));
        chipPens.setOnClickListener(v -> selectCategory(chipPens, 1));
        chipNotebooks.setOnClickListener(v -> selectCategory(chipNotebooks, 2));
        chipArt.setOnClickListener(v -> selectCategory(chipArt, 3));
        chipOffice.setOnClickListener(v -> selectCategory(chipOffice, 4));
        chipPaper.setOnClickListener(v -> selectCategory(chipPaper, 5));
    }

    private void selectCategory(TextView chip, int categoryId) {
        // Reset previous chip
        if (selectedChip != null) {
            selectedChip.setBackgroundResource(R.drawable.bg_chip_category);
            selectedChip.setTextColor(getColor(R.color.text_primary_light));
        }

        // Set new chip as selected
        chip.setBackgroundResource(R.drawable.bg_chip_selected);
        chip.setTextColor(getColor(R.color.white));
        selectedChip = chip;

        if (categoryId == 0) {
            productAdapter.setProducts(allProducts);
        } else {
            loadProductsByCategory(categoryId);
        }
    }

    private void loadFeaturedProducts() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body();
                    productAdapter.setProducts(allProducts);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductsByCategory(int categoryId) {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getProductsByCategory(categoryId).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productAdapter.setProducts(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error loading products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProductsBySearch(String query) {
        if (query.isEmpty()) {
            productAdapter.setProducts(allProducts);
            return;
        }

        List<Product> filtered = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    (product.getDescription() != null &&
                            product.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(product);
            }
        }
        productAdapter.setProducts(filtered);
    }

    private void openProductList() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onAddToCartClick(Product product) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        if (userId == 0) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("user_id", userId);
        body.put("product_id", product.getId());
        body.put("quantity", 1);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.addToCart(body).enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Added " + product.getName() + " to cart!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
}
