package com.example.convenient.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.convenient.R;

public class CartEmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart_empty);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnNext).setOnClickListener(v -> finish());


    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.transition.no_animation, R.transition.slide_out_down);
    }
}