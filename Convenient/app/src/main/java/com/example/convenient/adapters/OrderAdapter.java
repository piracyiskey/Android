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

import com.example.convenient.Activity.OrderDetailActivity;
import com.example.convenient.R;
import com.example.convenient.models.OrderSummary;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderSummary> orderList;

    public OrderAdapter(Context context, List<OrderSummary> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderSummary order = orderList.get(position);

        holder.txtOrderId.setText("#" + order.getOrderId());
        holder.txtCreatedDate.setText(order.getCreatedDate().replace("T", " "));
        holder.txtTotalPrice.setText(String.format("%,d VND", order.getTotalPrice()));

        // Set payment method icon
        switch (order.getPayMethod()) {
            case "momo":
                holder.imgPaymentMethod.setImageResource(R.drawable.ic_momo);
                break;
            case "zalopay":
                holder.imgPaymentMethod.setImageResource(R.drawable.ic_zalo);
                break;
            case "cash":
                holder.imgPaymentMethod.setImageResource(R.drawable.cash);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPaymentMethod;
        TextView txtOrderId, txtCreatedDate, txtTotalPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPaymentMethod = itemView.findViewById(R.id.imgPaymentMethod);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtCreatedDate = itemView.findViewById(R.id.txtCreatedDate);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
        }
    }
}
