package com.example.convenient.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.convenient.Activity.ProductDetailsActivity;
import com.example.convenient.models.OrderItem;
import com.example.convenient.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderItem> itemList;

    public OrderItemAdapter(Context context, List<OrderItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem item = itemList.get(position);
        holder.txtName.setText(item.getProductName());
        holder.txtPer.setText(item.getPer());
        holder.txtTotal.setText(formatCurrency(item.getTotal()));
        holder.txtQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context).load(item.getImageUrl()).into(holder.imgProduct);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            intent.putExtra("product_id", item.getProductId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPer, txtTotal, txtQuantity;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtProductName);
            txtPer = itemView.findViewById(R.id.txtPer);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
        }
    }

    private String formatCurrency(int amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " VND";
    }
}
