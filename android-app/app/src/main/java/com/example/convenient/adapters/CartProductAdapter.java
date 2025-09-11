package com.example.convenient.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.convenient.Activity.ProductDetailsActivity;
import com.example.convenient.R;
import com.example.convenient.models.CartItem;
import com.example.convenient.models.Product;

import java.io.IOException;
import java.util.List;

import okhttp3.*;

public class CartProductAdapter extends RecyclerView.Adapter<CartProductAdapter.CartViewHolder> {

    public interface CartUpdateListener {
        void onCartUpdated();
    }

    private final Context context;
    private final List<CartItem> items;
    private final OkHttpClient client = new OkHttpClient();
    private final String token;
    private final CartUpdateListener listener;

    public CartProductAdapter(Context context, List<CartItem> items, String token, CartUpdateListener listener) {
        this.context = context;
        this.items = items;
        this.token = token;
        this.listener = listener;
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPer, txtTotalPrice, txtQuantity, btnMinus, btnPlus;

        public CartViewHolder(View view) {
            super(view);
            imgProduct = view.findViewById(R.id.imgProduct);
            txtName = view.findViewById(R.id.txtName);
            txtPer = view.findViewById(R.id.txtPer);
            txtTotalPrice = view.findViewById(R.id.txtTotalPrice);
            txtQuantity = view.findViewById(R.id.txtQuantity);
            btnMinus = view.findViewById(R.id.btnMinus);
            btnPlus = view.findViewById(R.id.btnPlus);
        }

        public void bind(CartItem item) {
            Product product = item.getProduct();
            txtName.setText(product.getName());
            txtPer.setText(product.getPer());
            txtQuantity.setText(String.valueOf(item.getQuantity()));
            txtTotalPrice.setText(String.format("%,d VND", item.getTotal()));

            Glide.with(context).load(product.getImageUrl()).into(imgProduct);

            btnPlus.setOnClickListener(v -> updateQuantity(item, item.getQuantity() + 1));
            btnMinus.setOnClickListener(v -> {
                if (item.getQuantity() > 1)
                    updateQuantity(item, item.getQuantity() - 1);
            });

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailsActivity.class);
                intent.putExtra("product_id", product.getId());
                context.startActivity(intent);
            });
        }

        private void updateQuantity(CartItem item, int newQuantity) {
            item.setQuantity(newQuantity);
            item.setTotal(newQuantity * item.getProduct().getPrice());
            notifyItemChanged(getAdapterPosition());

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    "{\"productId\":\"" + item.getProduct().getId() + "\",\"quantity\":" + newQuantity + "}"
            );
            Request request = new Request.Builder()
                    .url("https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/cart/update")
                    .header("Authorization", "Bearer " + token)
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                public void onFailure(Call call, IOException e) { e.printStackTrace(); }
                public void onResponse(Call call, Response response) {
                    if (listener != null) {
                        listener.onCartUpdated();
                    }
                }
            });
        }
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void removeItem(int position) {
        CartItem removed = items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());

        Request request = new Request.Builder()
                .url("https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/cart/remove?productId=" + removed.getProduct().getId())
                .header("Authorization", "Bearer " + token)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }
            public void onResponse(Call call, Response response) {
                if (listener != null) {
                    listener.onCartUpdated();
                }
            }
        });
    }
}
