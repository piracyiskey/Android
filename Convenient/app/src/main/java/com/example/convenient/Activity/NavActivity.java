package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NavActivity extends AppCompatActivity {

    private static final String CART_COUNT_API_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/cart/count";

    BottomNavigationView bottomNavigationView;
    ImageButton btnCart;
    TextView cartBadge;
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        btnCart = findViewById(R.id.btn_cart);
        cartBadge = findViewById(R.id.cart_badge);

        loadFragment(new HomeFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) selected = new HomeFragment();
            else if (id == R.id.nav_profile) selected = new ProfileFragment();
            else if (id == R.id.nav_favorite) selected = new FavoriteFragment();

            if (selected != null)
                loadFragment(selected);

            return true;
        });

        btnCart.setOnClickListener(v -> checkCartAndNavigate());

        updateCartBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge(); // Refresh cart count every time this activity resumes
    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void updateCartBadge() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) return;

        Request request = new Request.Builder()
                .url(CART_COUNT_API_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        int count = json.getInt("count");

                        runOnUiThread(() -> {
                            if (count > 0) {
                                cartBadge.setVisibility(View.VISIBLE);
                                cartBadge.setText(String.valueOf(count));
                            } else {
                                cartBadge.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void checkCartAndNavigate() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) return;

        Request request = new Request.Builder()
                .url(CART_COUNT_API_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        int count = json.getInt("count");

                        runOnUiThread(() -> {
                            Intent intent = (count == 0)
                                    ? new Intent(NavActivity.this, CartEmptyActivity.class)
                                    : new Intent(NavActivity.this, CartActivity.class);

                            startActivity(intent);
                            overridePendingTransition(R.transition.slide_in_up, R.transition.no_animation);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
