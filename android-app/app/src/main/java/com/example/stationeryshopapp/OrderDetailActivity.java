package com.example.stationeryshopapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stationeryshopapp.api.ApiService;
import com.example.stationeryshopapp.api.RetrofitClient;
import com.example.stationeryshopapp.models.OrderDetailResponse;
import com.example.stationeryshopapp.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView txtTitle, txtStatusText, txtOrderDate, txtOrderTotal;
    private FrameLayout dotPending, dotShipped, dotDelivered;
    private View lineToShipped, lineToDelivered;
    private RecyclerView recyclerOrderItems;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        txtTitle = findViewById(R.id.txtTitle);
        txtStatusText = findViewById(R.id.txtStatusText);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtOrderTotal = findViewById(R.id.txtOrderTotal);
        dotPending = findViewById(R.id.dotPending);
        dotShipped = findViewById(R.id.dotShipped);
        dotDelivered = findViewById(R.id.dotDelivered);
        lineToShipped = findViewById(R.id.lineToShipped);
        lineToDelivered = findViewById(R.id.lineToDelivered);
        recyclerOrderItems = findViewById(R.id.recyclerOrderItems);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));

        int orderId = getIntent().getIntExtra("order_id", 0);
        if (orderId > 0) {
            txtTitle.setText("Order #" + orderId);
            loadOrderDetails(orderId);
        }
    }

    private void loadOrderDetails(int orderId) {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getOrderDetails(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderDetailResponse order = response.body();
                    displayOrder(order);
                }
            }

            @Override
            public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                Toast.makeText(OrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrder(OrderDetailResponse order) {
        txtOrderDate.setText("Ordered: " + formatDate(order.getOrderedAt()));
        txtOrderTotal.setText(String.format("Total: R%.2f", order.getTotalAmount()));

        // Update status tracker
        updateStatusTracker(order.getStatus());

        // Set items
        if (order.getItems() != null) {
            recyclerOrderItems.setAdapter(new OrderItemAdapter(order.getItems()));
        }
    }

    private void updateStatusTracker(String status) {
        // Reset all
        dotPending.setBackgroundResource(R.drawable.bg_chip_category);
        dotShipped.setBackgroundResource(R.drawable.bg_chip_category);
        dotDelivered.setBackgroundResource(R.drawable.bg_chip_category);
        lineToShipped.setBackgroundColor(getColor(R.color.divider_light));
        lineToDelivered.setBackgroundColor(getColor(R.color.divider_light));

        switch (status) {
            case "delivered":
                dotDelivered.setBackgroundResource(R.drawable.bg_chip_selected);
                lineToDelivered.setBackgroundColor(getColor(R.color.primary_violet));
                dotShipped.setBackgroundResource(R.drawable.bg_chip_selected);
                lineToShipped.setBackgroundColor(getColor(R.color.primary_violet));
                dotPending.setBackgroundResource(R.drawable.bg_chip_selected);
                txtStatusText.setText("Your order has been delivered!");
                break;
            case "shipped":
                dotShipped.setBackgroundResource(R.drawable.bg_chip_selected);
                lineToShipped.setBackgroundColor(getColor(R.color.primary_violet));
                dotPending.setBackgroundResource(R.drawable.bg_chip_selected);
                txtStatusText.setText("Your order is on its way");
                break;
            default:
                dotPending.setBackgroundResource(R.drawable.bg_chip_selected);
                txtStatusText.setText("Your order is being processed");
                break;
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null) return "";
        if (dateStr.length() >= 10) return dateStr.substring(0, 10);
        return dateStr;
    }

    // Inner adapter
    private class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ItemVH> {

        private final List<OrderItem> items;

        OrderItemAdapter(List<OrderItem> items) {
            this.items = items != null ? items : new ArrayList<>();
        }

        @NonNull
        @Override
        public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_detail_item, parent, false);
            return new ItemVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemVH holder, int position) {
            OrderItem item = items.get(position);
            holder.txtItemName.setText(item.getProductName());
            holder.txtItemQtyPrice.setText("Qty: " + item.getQuantity() + " x R" + String.format("%.2f", item.getUnitPrice()));
            holder.txtLineTotal.setText(String.format("R%.2f", item.getLineTotal()));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ItemVH extends RecyclerView.ViewHolder {
            TextView txtItemName, txtItemQtyPrice, txtLineTotal;

            ItemVH(@NonNull View itemView) {
                super(itemView);
                txtItemName = itemView.findViewById(R.id.txtItemName);
                txtItemQtyPrice = itemView.findViewById(R.id.txtItemQtyPrice);
                txtLineTotal = itemView.findViewById(R.id.txtLineTotal);
            }
        }
    }
}
