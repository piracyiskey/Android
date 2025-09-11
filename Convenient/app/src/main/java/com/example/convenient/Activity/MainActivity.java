package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPrefManager prefManager = new SharedPrefManager(this);

        if (prefManager.isFirstTimeLaunch()) {
            startActivity(new Intent(MainActivity.this, OnboardingActivity.class));
        } else {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        }

        finish(); // Prevent user from returning to this activity
    }
}