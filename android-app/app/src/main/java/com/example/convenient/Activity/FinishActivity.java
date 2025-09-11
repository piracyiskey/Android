package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.convenient.R;

public class FinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_finish);

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            startActivity(new Intent(this, NavActivity.class));
            overridePendingTransition(R.transition.no_animation, R.transition.slide_out_down);
        });

        findViewById(R.id.txtOrder).setOnClickListener(v -> {
            startActivity(new Intent(this, OrderActivity.class));
            overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });
    }
}