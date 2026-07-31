package com.example.stationeryshopapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stationeryshopapp.api.ApiService;
import com.example.stationeryshopapp.api.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText edtUsername, edtEmail, edtPhone, edtAddress;
    private MaterialButton btnSave;
    private ImageView btnBack;
    private TextView txtInitials, txtMemberSince;
    private LinearLayout linkOrders, linkWishlist, linkLogout;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        txtInitials = findViewById(R.id.txtInitials);
        txtMemberSince = findViewById(R.id.txtMemberSince);
        linkOrders = findViewById(R.id.linkOrders);
        linkWishlist = findViewById(R.id.linkWishlist);
        linkLogout = findViewById(R.id.linkLogout);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        userId = prefs.getInt("user_id", 0);

        linkOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
        });

        linkWishlist.setOnClickListener(v -> {
            startActivity(new Intent(this, WishlistActivity.class));
        });

        linkLogout.setOnClickListener(v -> logout());

        btnSave.setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    private void loadProfile() {
        if (userId == 0) return;

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getUserProfile(userId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> user = response.body();

                    String username = (String) user.get("username");
                    String email = (String) user.get("email");
                    String phone = (String) user.get("phone");
                    String address = (String) user.get("address");
                    String createdAt = (String) user.get("created_at");

                    edtUsername.setText(username);
                    edtEmail.setText(email);
                    edtPhone.setText(phone);
                    edtAddress.setText(address);

                    // Set initials
                    if (username != null && !username.isEmpty()) {
                        String initials = username.substring(0, Math.min(2, username.length())).toUpperCase();
                        txtInitials.setText(initials);
                    }

                    // Member since
                    if (createdAt != null && createdAt.length() >= 10) {
                        txtMemberSince.setText("Member since " + createdAt.substring(0, 10));
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String username = edtUsername.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (username.isEmpty()) {
            edtUsername.setError("Username is required");
            edtUsername.requestFocus();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("phone", phone);
        body.put("address", address);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.updateUserProfile(userId, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnSave.setEnabled(true);
                btnSave.setText("Save Changes");

                if (response.isSuccessful()) {
                    // Update SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", username);
                    editor.apply();

                    Toast.makeText(ProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText("Save Changes");
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
