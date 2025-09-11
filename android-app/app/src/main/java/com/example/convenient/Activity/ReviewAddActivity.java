package com.example.convenient.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReviewAddActivity extends AppCompatActivity {

    private ImageView[] stars = new ImageView[5];
    private double currentRating = 0;
    private EditText edtReview;
    private String productId;
    private OkHttpClient client;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_add);

        productId = getIntent().getStringExtra("product_id");
        client = new OkHttpClient();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        edtReview = findViewById(R.id.edtReview);
        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        for (int i = 0; i < stars.length; i++) {
            final int index = i;
            stars[i].setOnTouchListener((v, event) -> {
                float x = event.getX();
                float width = v.getWidth();
                if (x < width / 2) {
                    currentRating = index + 0.5;
                } else {
                    currentRating = index + 1.0;
                }
                updateStarDisplay();
                return false;
            });
        }

        findViewById(R.id.btnSubmit).setOnClickListener(v -> submitReview());
    }

    private void updateStarDisplay() {
        int fullStars = (int) currentRating;
        boolean half = currentRating - fullStars >= 0.5;

        for (int i = 0; i < 5; i++) {
            if (i < fullStars) {
                stars[i].setImageResource(R.drawable.ic_star_full);
            } else if (i == fullStars && half) {
                stars[i].setImageResource(R.drawable.ic_star_half);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_empty);
            }
        }
    }

    private void submitReview() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null || productId == null) {
            Toast.makeText(this, "Missing data", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewBody = edtReview.getText().toString().trim();
        if (reviewBody.isEmpty() || currentRating == 0) {
            Toast.makeText(this, "Please provide both rating and review", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("star", currentRating);
            json.put("body", reviewBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/ratings/" + productId + "/post")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ReviewAddActivity.this, "Failed to submit", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ReviewAddActivity.this, "Review submitted", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ReviewAddActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
