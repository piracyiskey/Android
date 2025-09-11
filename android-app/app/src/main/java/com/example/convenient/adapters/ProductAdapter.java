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
import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;
import com.example.convenient.models.Product;
import java.util.List;
import okhttp3.*;
import java.io.IOException;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    protected final Context context;
    protected final List<Product> productList;
    protected final String jwtToken;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.jwtToken = SharedPrefManager.getInstance(context).getToken();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        Glide.with(context).load(product.getImageUrl()).into(holder.imgProduct);
        holder.txtPrice.setText(product.getPrice() + " VND");
        holder.txtName.setText(product.getName());
        holder.txtPer.setText(product.getPer());

        updateHeartIcon(holder.imgHeart, product.isFavorite());

        holder.imgHeart.setOnClickListener(v -> {
            toggleFavorite(product); // ← only call this
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            intent.putExtra("product_id", product.getId());
            context.startActivity(intent);
        });
    }

    protected void updateHeartIcon(ImageView imgHeart, boolean isFavorite) {
        imgHeart.setImageResource(isFavorite ? R.drawable.ic_fav_filled : R.drawable.ic_heart);
    }

    // 👇 Make this method protected so subclass can override
    protected void toggleFavorite(Product product) {
        // Default behavior: update icon and send request
        boolean isFavorite = !product.isFavorite();
        product.setFavorite(isFavorite);

        // Notify UI
        notifyDataSetChanged();

        // Send request
        OkHttpClient client = new OkHttpClient();
        String url = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/favorites/" +
                (product.isFavorite() ? "add" : "remove") + "?productId=" + product.getId();
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + jwtToken)
                .method(product.isFavorite() ? "POST" : "DELETE",
                        product.isFavorite() ? RequestBody.create(new byte[0]) : null)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println("Favorite toggle failed: " + response.message());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, imgHeart;
        TextView txtPrice, txtName, txtPer;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imageProduct);
            imgHeart = itemView.findViewById(R.id.imageFavorite);
            txtPrice = itemView.findViewById(R.id.textPrice);
            txtName = itemView.findViewById(R.id.textName);
            txtPer = itemView.findViewById(R.id.textPer);
        }
    }
}
