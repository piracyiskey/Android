package com.example.convenient.Activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;
import com.example.convenient.adapters.OrderAdapter;
import com.example.convenient.models.OrderSummary;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<OrderSummary> orders = new ArrayList<>();
    private String token;

    private static final String ORDERS_API_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/orders";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        token = SharedPrefManager.getInstance(this).getToken();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchOrders();
    }

    private void fetchOrders() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(ORDERS_API_URL)
                .header("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(OrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonStr = response.body().string();
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<OrderSummary>>() {}.getType();
                    orders = gson.fromJson(jsonStr, listType);

                    runOnUiThread(() -> {
                        adapter = new OrderAdapter(OrderActivity.this, orders);
                        recyclerView.setAdapter(adapter);
                    });
                }
            }
        });
    }
}
