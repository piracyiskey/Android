// OrderDetailActivity.java
package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.convenient.adapters.OrderItemAdapter;
import com.example.convenient.models.OrderItem;
import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView title, txtSubtotal, txtDiscount, txtPaymentMethod, txtTotal;
    private RecyclerView recyclerView;
    private OrderItemAdapter adapter;
    private List<OrderItem> itemList = new ArrayList<>();
    private String orderId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        title = findViewById(R.id.title);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtDiscount = findViewById(R.id.txtDiscount);
        txtPaymentMethod = findViewById(R.id.txtPaymentMethod);
        txtTotal = findViewById(R.id.txtTotal);
        recyclerView = findViewById(R.id.recyclerViewProducts);
        ImageButton btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        orderId = getIntent().getStringExtra("orderId");
        title.setText("Order #" + orderId);

        btnBack.setOnClickListener(v -> finish());

        loadOrderDetails();
    }

    private void loadOrderDetails() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/orders/" + orderId + "/info";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + SharedPrefManager.getInstance(this).getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());

                        runOnUiThread(() -> {
                            try {
                                txtSubtotal.setText(formatCurrency(json.getInt("SubTotal")));
                                txtTotal.setText(formatCurrency(json.getInt("totalPrice")));

                                String discount = json.optString("discountApplied", "None");
                                txtDiscount.setText(discount.equals("null") ? "None" : discount);

                                txtPaymentMethod.setText(json.getString("payMethod"));

                                JSONArray itemsArray = json.getJSONArray("items");
                                itemList.clear();

                                for (int i = 0; i < itemsArray.length(); i++) {
                                    JSONObject itemObj = itemsArray.getJSONObject(i);
                                    OrderItem item = new OrderItem();
                                    item.setProductId(itemObj.getString("productId"));
                                    item.setProductName(itemObj.getString("productName"));
                                    item.setPer(itemObj.getString("per"));
                                    item.setQuantity(itemObj.getInt("quantity"));
                                    item.setImageUrl(itemObj.getString("imageUrl"));
                                    item.setTotal(itemObj.getInt("total"));
                                    itemList.add(item);
                                }

                                adapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String formatCurrency(int amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " VND";
    }
}
