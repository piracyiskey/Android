package com.example.convenient.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;
import com.example.convenient.adapters.ProductAdapter;
import com.example.convenient.models.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private final List<Product> productList = new ArrayList<>();
    private ProductAdapter adapter;
    private final OkHttpClient client = new OkHttpClient();
    private String jwtToken;

    private static final String URL_FAVORITES = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/favorites";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        jwtToken = SharedPrefManager.getInstance(requireContext()).getToken();

        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new ProductAdapter(requireContext(), productList) {
            @Override
            protected void toggleFavorite(Product product) {
                boolean newState = !product.isFavorite();
                product.setFavorite(newState);
                notifyDataSetChanged();

                String url = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/favorites/" +
                        (newState ? "add" : "remove") + "?productId=" + product.getId();
                Request request = new Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer " + jwtToken)
                        .method(newState ? "POST" : "DELETE", newState ? RequestBody.create(new byte[0]) : null)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override public void onResponse(@NonNull Call call, @NonNull Response response) {
                        if (!response.isSuccessful()) return;

                        if (!newState) {
                            requireActivity().runOnUiThread(() -> {
                                int index = productList.indexOf(product);
                                if (index >= 0) {
                                    View view = recyclerView.getLayoutManager().findViewByPosition(index);
                                    if (view != null) {
                                        Animation zoomOut = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_out);
                                        zoomOut.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {}

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                productList.remove(product);
                                                adapter.notifyItemRemoved(index);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {}
                                        });
                                        view.startAnimation(zoomOut);
                                    } else {
                                        // fallback in case view not found
                                        productList.remove(product);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }

                    }
                });
            }
        };


        recyclerView.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        Request request = new Request.Builder()
                .url(URL_FAVORITES)
                .header("Authorization", "Bearer " + jwtToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;

                String res = response.body().string();
                try {
                    JSONArray arr = new JSONArray(res);
                    productList.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        Product p = new Product();
                        p.setId(obj.optString("id"));
                        p.setName(obj.optString("name"));
                        p.setPer(obj.optString("per"));
                        p.setPrice(obj.optInt("price"));
                        p.setDesc(obj.optString("desc"));
                        p.setImageUrl(obj.optString("imageUrl"));
                        p.setFavorite(true); // All favorites
                        productList.add(p);
                    }

                    requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
