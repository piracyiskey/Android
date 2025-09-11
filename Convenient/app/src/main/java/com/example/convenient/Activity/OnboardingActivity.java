package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;
import com.example.convenient.adapters.OnboardingAdapter;
import com.example.convenient.models.OnboardingItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private LinearLayout layoutDots;
    private Button btnGetStarted;
    private final Handler handler = new Handler();
    private int currentPage = 0;
    private Runnable autoScrollRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager2 = findViewById(R.id.viewPager);
        layoutDots = findViewById(R.id.layoutDots);
        btnGetStarted = findViewById(R.id.btnGetStarted);

        List<OnboardingItem> onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem("Chào mừng đến với\n" + "MetroMini", "Tận hưởng trải nghiệm mua sắm tuyệt\n" + "vời tại đây", R.drawable.img1));
        onboardingItems.add(new OnboardingItem("Lựu chọn hàng đầu\n" + "của khách hàng ", "Nhanh chóng và thuận tiện, hoạt động\n" + "24/7", R.drawable.img2));
        onboardingItems.add(new OnboardingItem("Mua trái cây tươi\n" + "chất lượng", "Trái cây của chúng tôi luôn được thông qua\n" + "kiểm định trước khí bán", R.drawable.img3));
        onboardingItems.add(new OnboardingItem("Khuyến mãi trên\n" + "tất cả các mặt hàng", "Thỏa sức mua sắm và tiết kiệm với nhiều\n" + "ưu đãi hấp dẫn", R.drawable.img4));

        OnboardingAdapter onboardingAdapter = new OnboardingAdapter(onboardingItems);
        viewPager2.setAdapter(onboardingAdapter);

        // ✅ Add this line to set initial dots
        viewPager2.post(() -> setupDots(onboardingItems.size(), 0));

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(onboardingItems.size(), position);
                currentPage = position;
            }
        });

        SharedPrefManager prefManager = new SharedPrefManager(this);

        btnGetStarted.setOnClickListener(v -> {
            prefManager.setFirstTimeLaunch(false);

            startActivity(new Intent(OnboardingActivity.this, HomeActivity.class));
            overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
            finish();
        });

        autoScrollRunnable = () -> {
            currentPage = (currentPage + 1) % onboardingItems.size();
            viewPager2.setCurrentItem(currentPage, true);
            handler.postDelayed(autoScrollRunnable, 5000);
        };
        handler.postDelayed(autoScrollRunnable, 5000);
    }

    private void setupDots(int count, int position) {
        layoutDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            TextView dot = new TextView(this);
            dot.setText("●");
            dot.setTextSize(18);
            dot.setTextColor(getResources().getColor(
                    (i == position) ? R.color.active_dot : R.color.inactive_dot
            ));
            layoutDots.addView(dot);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoScrollRunnable);
    }
}
