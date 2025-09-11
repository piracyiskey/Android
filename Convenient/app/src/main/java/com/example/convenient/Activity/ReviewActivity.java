package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;
import com.example.convenient.adapters.ReviewAdapter;
import com.example.convenient.models.Review;

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

public class ReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private List<Review> reviewList;
    private String productId;

    // ✅ Activity result launcher for ReviewAddActivity
    private ActivityResultLauncher<Intent> reviewAddLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        productId = getIntent().getStringExtra("product_id");

        recyclerView = findViewById(R.id.recyclerViewReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(this, reviewList);
        recyclerView.setAdapter(adapter);

        ImageButton btnAdd = findViewById(R.id.btnAdd);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // ✅ Register launcher
        reviewAddLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadReviews();  // Reload when result is OK
                    }
                }
        );

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ReviewActivity.this, ReviewAddActivity.class);
            intent.putExtra("product_id", productId);
            reviewAddLauncher.launch(intent);  // ✅ Use launcher instead of startActivity
        });

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Product ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadReviews();
    }

    private void loadReviews() {
        String token = SharedPrefManager.getInstance(this).getToken();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/ratings/" + productId)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ReviewActivity.this, "Failed to load reviews", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(ReviewActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    reviewList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Review review = new Review(
                                obj.getString("profileURL"),
                                obj.getString("userName"),
                                obj.getDouble("star"),
                                obj.getString("body"),
                                obj.getString("createdDate")
                        );
                        reviewList.add(review);
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(ReviewActivity.this, "Parsing error", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
