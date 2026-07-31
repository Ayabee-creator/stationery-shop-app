package com.example.stationeryshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stationeryshopapp.adapters.ProductAdapter;
import com.example.stationeryshopapp.api.ApiService;
import com.example.stationeryshopapp.api.RetrofitClient;
import com.example.stationeryshopapp.models.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerProducts;
    private ProductAdapter productAdapter;
    private EditText edtSearchProducts;
    private TextView txtResults;
    private ImageView btnBack;

    private TextView chipAll, chipPens, chipNotebooks, chipArt, chipOffice, chipPaper;
    private TextView selectedChip;

    private List<Product> allProducts = new ArrayList<>();
    private int currentCategoryId = 0; // 0 = All

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerProducts = findViewById(R.id.recyclerProducts);
        edtSearchProducts = findViewById(R.id.edtSearchProducts);
        txtResults = findViewById(R.id.txtResults);
        btnBack = findViewById(R.id.btnBack);

        chipAll = findViewById(R.id.chipAll);
        chipPens = findViewById(R.id.chipPens);
        chipNotebooks = findViewById(R.id.chipNotebooks);
        chipArt = findViewById(R.id.chipArt);
        chipOffice = findViewById(R.id.chipOffice);
        chipPaper = findViewById(R.id.chipPaper);

        // Setup RecyclerView with grid layout (2 columns)
        productAdapter = new ProductAdapter(this);
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setAdapter(productAdapter);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Category chip click handlers
        selectedChip = chipAll;
        setupChipClickListeners();

        // Search functionality
        edtSearchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProductsBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load all products
        loadProducts();
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

        currentCategoryId = categoryId;

        if (categoryId == 0) {
            productAdapter.setProducts(allProducts);
            txtResults.setText(allProducts.size() + " products found");
        } else {
            loadProductsByCategory(categoryId);
        }
    }

    private void loadProducts() {
        txtResults.setText("Loading products...");

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body();
                    productAdapter.setProducts(allProducts);
                    txtResults.setText(allProducts.size() + " products found");
                } else {
                    txtResults.setText("Failed to load products");
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                txtResults.setText("Error: " + t.getMessage());
            }
        });
    }

    private void loadProductsByCategory(int categoryId) {
        txtResults.setText("Loading...");

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getProductsByCategory(categoryId).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> filtered = response.body();
                    productAdapter.setProducts(filtered);
                    txtResults.setText(filtered.size() + " products found");
                } else {
                    txtResults.setText("No products in this category");
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                txtResults.setText("Error: " + t.getMessage());
            }
        });
    }

    private void filterProductsBySearch(String query) {
        if (query.isEmpty()) {
            if (currentCategoryId == 0) {
                productAdapter.setProducts(allProducts);
                txtResults.setText(allProducts.size() + " products found");
            } else {
                loadProductsByCategory(currentCategoryId);
            }
            return;
        }

        List<Product> filtered = new ArrayList<>();
        List<Product> sourceList = allProducts;

        for (Product product : sourceList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    (product.getDescription() != null &&
                            product.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(product);
            }
        }

        productAdapter.setProducts(filtered);
        txtResults.setText(filtered.size() + " products found");
    }

    @Override
    public void onAddToCartClick(Product product) {
        android.content.SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
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
        apiService.addToCart(body).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(Call<java.util.Map<String, Object>> call, Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Added " + product.getName() + " to cart!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
}
