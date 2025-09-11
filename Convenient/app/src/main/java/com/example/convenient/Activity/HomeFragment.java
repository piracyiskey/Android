package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;
import com.example.convenient.adapters.ImageSliderAdapter;
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

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private LinearLayout layoutDots, btnCategory, btnHot;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());

    private RecyclerView recyclerViewHot;
    private ProductAdapter hotAdapter;
    private final List<Product> hotProductList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    private static final String URL_HOT_PRODUCTS = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/products/hot";

    private final int[] images = {
            R.drawable.slider1,
            R.drawable.slider2,
            R.drawable.slider3,
            R.drawable.slider4
    };

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int nextItem = (viewPager.getCurrentItem() + 1) % images.length;
            viewPager.setCurrentItem(nextItem, true);
            sliderHandler.postDelayed(this, 5000);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        viewPager = root.findViewById(R.id.viewPager);
        layoutDots = root.findViewById(R.id.dotsIndicator);
        btnCategory = root.findViewById(R.id.btnCategory);
        btnHot = root.findViewById(R.id.btnHot);
        recyclerViewHot = root.findViewById(R.id.recyclerViewHotProducts);
        EditText edtSearch = root.findViewById(R.id.edtSearch);
        ImageButton btnSearch = root.findViewById(R.id.btnSearch);

        View.OnClickListener performSearch = v -> {
            String query = edtSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                Intent intent = new Intent(requireContext(), SearchActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
            }else{
                Toast.makeText(requireContext(), "Vui lòng nhập từ khóa", Toast.LENGTH_SHORT).show();
            }
        };

        btnSearch.setOnClickListener(performSearch);

        // Handle "Enter" key press from keyboard
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch.onClick(null);
            return true;
        });

        btnCategory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CategoryActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });

        btnHot.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProductHotActivity.class);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });

        setupCategoryClick(root, R.id.btnVeggie, "Rau củ");
        setupCategoryClick(root, R.id.btnFruit, "Trái cây");
        setupCategoryClick(root, R.id.btnDrink, "Đồ uống");
        setupCategoryClick(root, R.id.btnSnack, "Đồ ăn nhẹ");
        setupCategoryClick(root, R.id.btnSpice, "Gia vị");
        setupCategoryClick(root, R.id.btnHygiene, "Vệ sinh");
        setupCategoryClick(root, R.id.btnGrocery, "Hàng tạp hóa");

        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        viewPager.setAdapter(adapter);
        viewPager.post(() -> setupDots(images.length, 0));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(images.length, position);
            }
        });

        recyclerViewHot.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        hotAdapter = new ProductAdapter(requireContext(), hotProductList);
        recyclerViewHot.setAdapter(hotAdapter);

        loadHotProducts();
        sliderHandler.postDelayed(sliderRunnable, 5000);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHotProducts();
    }

    private void setupDots(int count, int position) {
        layoutDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            TextView dot = new TextView(requireContext());
            dot.setText("●");
            dot.setTextSize(18);
            int colorResId = (i == position) ? R.color.active_dot : R.color.inactive_dot;
            dot.setTextColor(ContextCompat.getColor(requireContext(), colorResId));
            layoutDots.addView(dot);
        }
    }

    private void setupCategoryClick(View root, int buttonId, String categoryName) {
        LinearLayout button = root.findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProductListActivity.class);
            intent.putExtra("category_name", categoryName);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });
    }

    private void loadHotProducts() {
        String token = SharedPrefManager.getInstance(requireContext()).getToken();

        Request request = new Request.Builder()
                .url(URL_HOT_PRODUCTS)
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
                                obj.optBoolean("favorite", false) // API now returns this
                        );
                        tempProducts.add(product);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                requireActivity().runOnUiThread(() -> {
                    hotProductList.clear();
                    hotProductList.addAll(tempProducts);
                    hotAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
