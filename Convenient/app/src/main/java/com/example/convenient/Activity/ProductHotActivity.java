package com.example.convenient.Activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;
import com.example.convenient.adapters.ProductAdapter;
import com.example.convenient.models.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class ProductHotActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<Product> productHot = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    private static final String API_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/products/hot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_hot);

        ImageButton btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, productHot);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());


        loadProducts();
    }

    private void loadProducts() {
        String token = SharedPrefManager.getInstance(this).getToken();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;

                String json = response.body().string();
                List<Product> tempProducts = new ArrayList<>();
                try {
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Product product = new Product(
                                obj.getString("id"),
                                obj.getString("name"),
                                obj.getString("per"),
                                obj.getInt("price"),
                                obj.getString("desc"),
                                obj.getString("imageUrl"),
                                obj.getBoolean("hot"),
                                obj.getString("categoryName"),
                                obj.optBoolean("favorite", false) // now comes from API
                        );
                        tempProducts.add(product);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(() -> {
                    productHot.clear();
                    productHot.addAll(tempProducts);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }
}
