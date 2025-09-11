package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;
import com.example.convenient.Utils.StarRatingUtil;

import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView imgProduct, imgFavorite;
    private TextView txtPrice, txtName, txtPer, txtDescription, txtRatingSummary, btnMinus, btnPlus, edtAmount;
    private Button  btnAddToCart;

    View ratingLayout;

    private ImageButton btnBack;


    private String productId;
    private boolean isFavorite = false;
    private final OkHttpClient client = new OkHttpClient();
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        productId = getIntent().getStringExtra("product_id");
        jwtToken = SharedPrefManager.getInstance(this).getToken();

        imgProduct = findViewById(R.id.imgProduct);
        imgFavorite = findViewById(R.id.imgFavorite);
        txtPrice = findViewById(R.id.txtPrice);
        txtName = findViewById(R.id.txtName);
        txtPer = findViewById(R.id.txtPer);
        txtDescription = findViewById(R.id.txtDescription);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        edtAmount = findViewById(R.id.tvQuantity);
        btnBack = findViewById(R.id.btnBack);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        btnBack.setOnClickListener(v -> finish());

        edtAmount.setText("1");

        btnMinus.setOnClickListener(v -> {
            int amt = Integer.parseInt(edtAmount.getText().toString());
            if (amt > 1) edtAmount.setText(String.valueOf(amt - 1));
        });

        btnPlus.setOnClickListener(v -> {
            int amt = Integer.parseInt(edtAmount.getText().toString());
            edtAmount.setText(String.valueOf(amt + 1));
        });

        btnAddToCart.setOnClickListener(v -> {
            int quantity = Integer.parseInt(edtAmount.getText().toString());

            JSONObject json = new JSONObject();
            try {
                json.put("productId", productId);
                json.put("quantity", quantity);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/cart/add")
                    .header("Authorization", "Bearer " + jwtToken)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ProductDetailsActivity.this, "Không thể thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(ProductDetailsActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProductDetailsActivity.this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });




        imgFavorite.setOnClickListener(v -> toggleFavorite());

        ratingLayout = findViewById(R.id.ratingLayout);

        ratingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailsActivity.this, ReviewActivity.class);
            intent.putExtra("product_id", productId);
            startActivity(intent);
        });


        loadProductDetails();
        loadRatingSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRatingSummary();
    }

    private void loadProductDetails() {
        Request request = new Request.Builder()
                .url("https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/products/" + productId)
                .header("Authorization", "Bearer " + jwtToken) // 🔒 ADD THIS
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String res = response.body().string();
                try {
                    JSONObject obj = new JSONObject(res);
                    runOnUiThread(() -> {
                        Glide.with(ProductDetailsActivity.this).load(obj.optString("imageUrl")).into(imgProduct);
                        txtPrice.setText(obj.optInt("price") + " VND");
                        txtName.setText(obj.optString("name"));
                        txtPer.setText(obj.optString("per"));
                        txtDescription.setText(obj.optString("desc"));
                        isFavorite = obj.optBoolean("favorite");
                        updateFavoriteIcon();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadRatingSummary() {
        Request request = new Request.Builder()
                .url("https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/ratings/" + productId + "/summary")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String res = response.body().string();
                try {
                    JSONObject obj = new JSONObject(res);
                    double avg = obj.optDouble("average", 0);
                    int count = obj.optInt("count", 0);
                    runOnUiThread(() -> {
                        ((TextView) ratingLayout.findViewById(R.id.txtAverage)).setText(String.format("%.1f", avg));
                        ((TextView) ratingLayout.findViewById(R.id.txtReviewCount)).setText("(" + count + " đánh giá)");
                        StarRatingUtil.setStarRating(ratingLayout, avg);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private void toggleFavorite() {
        isFavorite = !isFavorite;
        updateFavoriteIcon();

        String url = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/favorites/" + (isFavorite ? "add" : "remove") + "?productId=" + productId;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + jwtToken)
                .method(isFavorite ? "POST" : "DELETE", isFavorite ? RequestBody.create(new byte[0]) : null)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { e.printStackTrace(); }
            @Override public void onResponse(Call call, Response response) { /* No UI feedback needed */ }
        });
    }

    private void updateFavoriteIcon() {
        imgFavorite.setImageResource(isFavorite ? R.drawable.ic_fav_filled : R.drawable.ic_heart);
    }
}
