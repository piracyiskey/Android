package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convenient.R;
import com.example.convenient.adapters.CategoryAdapter;
import com.example.convenient.models.Category;

import java.util.Arrays;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerCategories;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        recyclerCategories = findViewById(R.id.recyclerCategories);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        List<Category> categories = Arrays.asList(
                new Category(1, "Rau củ", R.drawable.ic_veggie, R.color.ca01),
                new Category(2, "Trái cây", R.drawable.ic_fruit, R.color.ca02),
                new Category(3, "Đồ uống", R.drawable.ic_drink, R.color.ca03),
                new Category(4, "Đồ ăn nhẹ", R.drawable.ic_snack, R.color.ca04),
                new Category(5, "Gia vị", R.drawable.ic_spice, R.color.ca05),
                new Category(6, "Vệ sinh", R.drawable.ic_hygiene, R.color.ca06),
                new Category(7, "Hàng tạp hóa", R.drawable.ic_grocery, R.color.ca07)
        );

        recyclerCategories.setLayoutManager(new GridLayoutManager(this, 3));
        CategoryAdapter adapter = new CategoryAdapter(this, categories, category -> {
            // Navigate to product list filtered by this category
            Intent intent = new Intent(this, ProductListActivity.class);
            intent.putExtra("category_name", category.title);
            startActivity(intent);
        });
        recyclerCategories.setAdapter(adapter);
    }
}
