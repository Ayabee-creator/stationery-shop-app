package com.example.stationeryshopapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView txtWelcome;
    private Button btnProducts, btnOrders, btnProfile, btnLogout;

    private LinearLayout cardPen, cardNotebook, cardPencil, cardPaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtWelcome = findViewById(R.id.txtWelcome);

        btnProducts = findViewById(R.id.btnProducts);
        btnOrders = findViewById(R.id.btnOrders);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        cardPen = findViewById(R.id.cardPen);
        cardNotebook = findViewById(R.id.cardNotebook);
        cardPencil = findViewById(R.id.cardPencil);
        cardPaper = findViewById(R.id.cardPaper);

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");

        txtWelcome.setText("Welcome, " + username);

        btnProducts.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnOrders.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Orders screen will be added next", Toast.LENGTH_SHORT).show();
        });

        btnProfile.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Profile screen will be added next", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> logoutUser());

        cardPen.setOnClickListener(v -> showOrderOption(1, "Blue Ballpoint Pen", "R1.99"));
        cardNotebook.setOnClickListener(v -> showOrderOption(4, "A5 Lined Notebook", "R6.99"));
        cardPencil.setOnClickListener(v -> showOrderOption(2, "HB Pencil Pack", "R3.49"));
        cardPaper.setOnClickListener(v -> showOrderOption(10, "A4 Printing Paper", "R5.99"));
    }

    private void showOrderOption(int productId, String productName, String price) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(productName);
        builder.setMessage("Price: " + price + "\n\nWould you like to order this product?");

        builder.setPositiveButton("Order Now", (dialog, which) -> {
            Toast.makeText(
                    HomeActivity.this,
                    "Order option selected for " + productName,
                    Toast.LENGTH_SHORT
            ).show();

            // Next step: here we will call POST /orders using Retrofit
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
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
}