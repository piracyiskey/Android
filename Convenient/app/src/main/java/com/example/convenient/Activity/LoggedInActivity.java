package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.convenient.R;
import com.example.convenient.Utils.HttpClientInstance;
import com.example.convenient.Utils.SharedPrefManager;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class LoggedInActivity extends AppCompatActivity {

    private static final String PROFILE_URL = "http://192.168.56.1:9090/api/v1/user/me";

    TextView txtEmail, txtFullName;
    LinearLayout btnLogout;
    ImageView imgProfilePic;

    SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        txtEmail = findViewById(R.id.txtEmail);
        txtFullName = findViewById(R.id.txtFullName);
        btnLogout = findViewById(R.id.btnLogout);
        imgProfilePic = findViewById(R.id.imgProfilePic);

        prefManager = new SharedPrefManager(this);

        fetchUserProfile();

        btnLogout.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(LoggedInActivity.this);
            builder.setMessage("Bạn có muốn đăng xuất?")
                    .setCancelable(true)
                    .setPositiveButton("Có", (dialog, id) -> {
                        prefManager.clearToken();
                        startActivity(new Intent(LoggedInActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("Hủy", (dialog, id) -> dialog.dismiss());

            androidx.appcompat.app.AlertDialog alert = builder.create();
            alert.show();
        });
    }

    private void fetchUserProfile() {
        String token = prefManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Request request = new Request.Builder()
                .url(PROFILE_URL)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        HttpClientInstance.getInstance().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoggedInActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoggedInActivity.this, "Phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                        prefManager.clearToken();
                        startActivity(new Intent(LoggedInActivity.this, LoginActivity.class));
                        finish();
                    });
                    return;
                }

                String json = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject obj = new JSONObject(json);
                        txtEmail.setText("Email: " + obj.getString("email"));
                        txtFullName.setText("Tên: " + obj.getString("full_name"));

                        String profilePicName = obj.optString("profile_pic", "default_profile_pic");
                        int resID = getResources().getIdentifier(profilePicName.split("\\.")[0], "drawable", getPackageName());

                        Glide.with(LoggedInActivity.this)
                                .load(resID != 0 ? resID : R.drawable.default_profile_pic)
                                .transform(new CircleCrop())
                                .into(imgProfilePic);
                    } catch (Exception e) {
                        Toast.makeText(LoggedInActivity.this, "Lỗi khi phân tích phản hồi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
