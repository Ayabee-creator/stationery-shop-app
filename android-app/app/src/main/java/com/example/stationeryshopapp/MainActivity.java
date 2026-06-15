package com.example.stationeryshopapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stationeryshopapp.api.ApiService;
import com.example.stationeryshopapp.api.RetrofitClient;
import com.example.stationeryshopapp.models.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnLoadProducts;
    private TextView txtResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoadProducts = findViewById(R.id.btnLoadProducts);
        txtResults = findViewById(R.id.txtResults);

        btnLoadProducts.setOnClickListener(v -> loadProducts());
    }

    private void loadProducts() {
        txtResults.setText("Loading products...");

        ApiService apiService = RetrofitClient.getApiService();

        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();

                    StringBuilder builder = new StringBuilder();

                    for (Product product : products) {
                        builder.append("ID: ").append(product.getId()).append("\n");
                        builder.append("Name: ").append(product.getName()).append("\n");
                        builder.append("Category: ").append(product.getCategory()).append("\n");
                        builder.append("Price: R").append(product.getPrice()).append("\n");
                        builder.append("Stock: ").append(product.getStockQuantity()).append("\n");
                        builder.append("-------------------------\n");
                    }

                    txtResults.setText(builder.toString());
                } else {
                    txtResults.setText("Failed to load products. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                txtResults.setText("Error: " + t.getMessage());
            }
        });
    }
}