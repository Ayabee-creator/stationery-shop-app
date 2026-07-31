package com.example.stationeryshopapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stationeryshopapp.api.ApiService;
import com.example.stationeryshopapp.api.RetrofitClient;
import com.example.stationeryshopapp.models.Order;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private TextView txtEmpty;
    private ImageView btnBack;
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        recyclerOrders = findViewById(R.id.recyclerOrders);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        orderAdapter = new OrderAdapter();
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(orderAdapter);

        loadOrders();
    }

    private void loadOrders() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", 0);

        if (userId == 0) {
            txtEmpty.setText("Please log in to view orders");
            txtEmpty.setVisibility(View.VISIBLE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getOrdersByUser(userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    if (orders.isEmpty()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                        recyclerOrders.setVisibility(View.GONE);
                    } else {
                        txtEmpty.setVisibility(View.GONE);
                        recyclerOrders.setVisibility(View.VISIBLE);
                        orderAdapter.setOrders(orders);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(OrderHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Inner adapter class
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

        private List<Order> orders = new ArrayList<>();

        void setOrders(List<Order> orders) {
            this.orders = orders;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_card, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.bind(order);
        }

        @Override
        public int getItemCount() { return orders.size(); }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView txtOrderId, txtStatus, txtOrderDate, txtItemCount, txtTotal, txtViewDetails;

            OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                txtOrderId = itemView.findViewById(R.id.txtOrderId);
                txtStatus = itemView.findViewById(R.id.txtStatus);
                txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
                txtItemCount = itemView.findViewById(R.id.txtItemCount);
                txtTotal = itemView.findViewById(R.id.txtTotal);
                txtViewDetails = itemView.findViewById(R.id.txtViewDetails);
            }

            void bind(Order order) {
                txtOrderId.setText("Order #" + order.getId());
                txtStatus.setText(capitalize(order.getStatus()));
                txtOrderDate.setText(formatDate(order.getOrderedAt()));
                txtItemCount.setText(order.getItemCount() + " items");
                txtTotal.setText(String.format("R%.2f", order.getTotalAmount()));

                // Status badge color
                switch (order.getStatus()) {
                    case "delivered":
                        txtStatus.setBackgroundResource(R.drawable.bg_banner_student);
                        txtStatus.setTextColor(getColor(R.color.banner_text));
                        break;
                    case "shipped":
                        txtStatus.setBackgroundResource(R.drawable.bg_badge_category);
                        txtStatus.setTextColor(getColor(R.color.accent_cyan_dark));
                        break;
                    default:
                        txtStatus.setBackgroundResource(R.drawable.bg_chip_selected);
                        txtStatus.setTextColor(getColor(R.color.white));
                        break;
                }

                txtViewDetails.setOnClickListener(v -> {
                    Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
                    intent.putExtra("order_id", order.getId());
                    startActivity(intent);
                });

                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailActivity.class);
                    intent.putExtra("order_id", order.getId());
                    startActivity(intent);
                });
            }
        }
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private String formatDate(String dateStr) {
        if (dateStr == null) return "";
        // Simple format: just return the first 10 chars (date portion)
        if (dateStr.length() >= 10) {
            return dateStr.substring(0, 10);
        }
        return dateStr;
    }
}
