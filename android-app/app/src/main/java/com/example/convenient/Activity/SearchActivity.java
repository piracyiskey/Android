package com.example.convenient.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    private TextView title;
    private RecyclerView recyclerView;
    private final List<Product> productList = new ArrayList<>();
    private ProductAdapter adapter;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        title = findViewById(R.id.title);
        recyclerView = findViewById(R.id.recyclerViewProducts);
        ImageButton btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        String query = getIntent().getStringExtra("query");
        if (query != null && !query.trim().isEmpty()) {
            title.setText("Kết quả cho \"" + query + "\"");
            searchProducts(query.trim());
        }
    }

    private void searchProducts(String keyword) {
        String token = SharedPrefManager.getInstance(this).getToken();
        String url = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/products/search?name=" + keyword;

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(SearchActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        List<Product> tempList = new ArrayList<>();
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
                                    obj.optBoolean("favorite", false)
                            );
                            tempList.add(product);
                        }

                        runOnUiThread(() -> {
                            productList.clear();
                            productList.addAll(tempList);
                            adapter.notifyDataSetChanged();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        showNotFoundToast();
                    }
                } else {
                    showNotFoundToast();
                }
            }
        });
    }

    private void showNotFoundToast() {
        runOnUiThread(() -> Toast.makeText(SearchActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show());
    }
}
