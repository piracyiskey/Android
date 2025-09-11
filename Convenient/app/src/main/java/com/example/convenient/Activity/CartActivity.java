package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;
import com.example.convenient.adapters.CartProductAdapter;
import com.example.convenient.models.CartItem;
import com.example.convenient.models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CartActivity extends AppCompatActivity implements CartProductAdapter.CartUpdateListener {

    private RecyclerView recyclerView;
    private CartProductAdapter adapter;
    private List<CartItem> cartItems;

    private TextView txtSubtotal, txtDiscount, txtTotal;
    private EditText edtVoucher;
    private Button btnApplyVoucher, btnNext;
    private ProgressBar progressBar;

    private int subtotal = 0;
    private int discount = 0;
    private String discountType = null;
    private String voucherCode = null;

    private String token;
    private static final String CART_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/cart";
    private static final String VOUCHER_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/cart/voucher";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerCart);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtDiscount = findViewById(R.id.txtDiscount);
        txtTotal = findViewById(R.id.txtTotal);
        edtVoucher = findViewById(R.id.edtVoucher);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressCart);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        token = SharedPrefManager.getInstance(this).getToken();
        cartItems = new ArrayList<>();

        adapter = new CartProductAdapter(this, cartItems, token, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                return false;
            }

            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                adapter.removeItem(vh.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        btnApplyVoucher.setOnClickListener(v -> applyVoucher());

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
            if (voucherCode != null) {
                intent.putExtra("voucherCode", voucherCode);
            }
            else {
                intent.putExtra("voucherCode", "");
            }
            intent.putExtra("finalTotal", txtTotal.getText().toString());
            startActivity(intent);
        });

        loadCartItems();
    }

    private void loadCartItems() {
        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(CART_URL)
                .header("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CartActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
                });
            }

            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                try {
                    JSONObject json = new JSONObject(jsonData);
                    JSONArray itemsArray = json.getJSONArray("items");

                    cartItems.clear();
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject obj = itemsArray.getJSONObject(i);
                        JSONObject prodJson = obj.getJSONObject("product");

                        CartItem item = new CartItem();
                        Product prod = new Product();

                        prod.setId(prodJson.getString("id"));
                        prod.setName(prodJson.getString("name"));
                        prod.setPer(prodJson.getString("per"));
                        prod.setPrice(prodJson.getInt("price"));
                        prod.setDesc(prodJson.getString("desc"));
                        prod.setImageUrl(prodJson.getString("imageUrl"));
                        prod.setHot(prodJson.getBoolean("hot"));
                        prod.setCategoryName(prodJson.getString("categoryName"));

                        item.setProduct(prod);
                        item.setQuantity(obj.getInt("quantity"));
                        item.setTotal(obj.getInt("total"));

                        cartItems.add(item);
                    }

                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();

                        if (cartItems.isEmpty()) {
                            Intent intent = new Intent(CartActivity.this, CartEmptyActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.transition.no_animation, R.transition.no_animation);
                            finish();
                            return;
                        }

                        try {
                            subtotal = json.getInt("totalPrice");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        txtSubtotal.setText(String.format("%,d VND", subtotal));
                        recalculateTotal();
                    });


                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(CartActivity.this, "Error parsing cart", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void applyVoucher() {
        String code = edtVoucher.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Enter a voucher code", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("voucherCode", code);
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(VOUCHER_URL)
                .header("Authorization", "Bearer " + token)
                .post(okhttp3.RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CartActivity.this, "Failed to apply voucher", Toast.LENGTH_SHORT).show());
            }

            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                if (!response.isSuccessful()) {
                    try {
                        JSONObject errorJson = new JSONObject(body);
                        String errorMsg = errorJson.optString("error", "Invalid voucher");
                        runOnUiThread(() -> Toast.makeText(CartActivity.this, errorMsg, Toast.LENGTH_SHORT).show());
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(CartActivity.this, "Invalid voucher", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                try {
                    JSONObject json = new JSONObject(body);
                    discountType = json.getString("type");
                    voucherCode = code;

                    if ("PERCENTAGE".equals(discountType)) {
                        int percent = json.getInt("value");
                        discount = subtotal * percent / 100;
                        runOnUiThread(() -> txtDiscount.setText(percent + " %"));
                    } else {
                        discount = json.getInt("value");
                        runOnUiThread(() -> txtDiscount.setText(String.format("%,d VND", discount)));
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(CartActivity.this, "Voucher applied", Toast.LENGTH_SHORT).show();
                        recalculateTotal();
                    });

                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(CartActivity.this, "Response error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private void recalculateTotal() {
        int total = Math.max(0, subtotal - discount);
        txtTotal.setText(String.format("%,d VND", total));
    }

    @Override
    public void onCartUpdated() {
        runOnUiThread(this::loadCartItems);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.transition.no_animation, R.transition.slide_out_down);
    }
}
