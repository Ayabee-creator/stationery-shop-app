package com.example.stationeryshopapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.stationeryshopapp.R;
import com.example.stationeryshopapp.models.Product;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onAddToCartClick(Product product);
        void onProductClick(Product product);
    }

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public void filterProducts(List<Product> filteredProducts) {
        this.products = filteredProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;
        private final TextView txtProductName;
        private final TextView txtCategory;
        private final TextView txtDescription;
        private final TextView txtPrice;
        private final TextView txtStockBadge;
        private final MaterialButton btnAddToCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtStockBadge = itemView.findViewById(R.id.txtStockBadge);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }

        void bind(Product product) {
            txtProductName.setText(product.getName());
            txtCategory.setText(product.getCategory());
            txtPrice.setText(String.format("R%.2f", product.getPrice()));

            // Load product image with Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageUrl())
                        .transform(new CenterCrop(), new RoundedCorners(24))
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.mipmap.ic_launcher);
            }

            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                txtDescription.setText(product.getDescription());
                txtDescription.setVisibility(View.VISIBLE);
            } else {
                txtDescription.setVisibility(View.GONE);
            }

            // Stock badge
            if (product.getStockQuantity() > 20) {
                txtStockBadge.setText("In Stock");
                txtStockBadge.setVisibility(View.VISIBLE);
            } else if (product.getStockQuantity() > 0) {
                txtStockBadge.setText("Low Stock");
                txtStockBadge.setVisibility(View.VISIBLE);
            } else {
                txtStockBadge.setText("Out of Stock");
                txtStockBadge.setVisibility(View.VISIBLE);
            }

            btnAddToCart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClick(product);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
