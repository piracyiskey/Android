package com.example.convenient.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.convenient.Utils.SharedPrefManager;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


import com.example.convenient.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.*;

public class PaymentActivity extends AppCompatActivity {
    private Button btnMomo, btnCash, btnZalo, btnPlaceOrder;
    private String voucherCode, finalTotal, selectedMethod = "momo";
    private String token;

    private static final String PLACE_ORDER_API_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/orders/checkout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        btnMomo = findViewById(R.id.btnMomo);
        btnCash = findViewById(R.id.btnCash);
        btnZalo = findViewById(R.id.btnZalo);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        token = SharedPrefManager.getInstance(this).getToken();
        voucherCode = getIntent().getStringExtra("voucherCode");
        finalTotal = getIntent().getStringExtra("finalTotal");

        showFragment(MomoFragment.newInstance(finalTotal));
        highlightTab(btnMomo);

        btnMomo.setOnClickListener(v -> {
            selectedMethod = "momo";
            showFragment(MomoFragment.newInstance(finalTotal));
            highlightTab(btnMomo);
        });

        btnCash.setOnClickListener(v -> {
            selectedMethod = "cash";
            showFragment(CashFragment.newInstance(finalTotal));
            highlightTab(btnCash);
        });

        btnZalo.setOnClickListener(v -> {
            selectedMethod = "zalopay";
            showFragment(ZaloFragment.newInstance(finalTotal));
            highlightTab(btnZalo);
        });

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void highlightTab(Button selected) {
        btnMomo.setBackgroundResource(R.drawable.tab_unselected);
        btnCash.setBackgroundResource(R.drawable.tab_unselected);
        btnZalo.setBackgroundResource(R.drawable.tab_unselected);

        btnMomo.setTextColor(Color.BLACK);
        btnCash.setTextColor(Color.BLACK);
        btnZalo.setTextColor(Color.BLACK);

        selected.setBackgroundResource(R.drawable.tab_selected);
        selected.setTextColor(Color.WHITE);
    }


    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.paymentFragmentContainer, fragment)
                .commit();
    }

    private void placeOrder() {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            if (voucherCode != null) json.put("voucherCode", voucherCode);
            json.put("payMethod", selectedMethod);
        } catch (JSONException e) {
            Toast.makeText(this, "Error preparing order", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(PLACE_ORDER_API_URL)
                .header("Authorization", "Bearer " + token)
                .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PaymentActivity.this, "Order failed", Toast.LENGTH_SHORT).show());
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    startActivity(new Intent(PaymentActivity.this, FinishActivity.class));
                    finish();
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(PaymentActivity.this, "Order error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
