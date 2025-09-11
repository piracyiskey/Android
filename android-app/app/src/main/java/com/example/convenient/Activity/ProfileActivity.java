package com.example.convenient.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.convenient.R;
import com.example.convenient.Utils.SharedPrefManager;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1";
    private static final String GET_USER_URL = BASE_URL + "/user/me";
    private static final String UPDATE_PROFILE_URL = BASE_URL + "/user/update-profile";
    private static final String LOGIN_URL = BASE_URL + "/user/login";
    private static final String UPDATE_PASSWORD_URL = BASE_URL + "/user/update-password";

    EditText edtName, edtEmail, edtPhone, edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    Button btnSave;

    OkHttpClient client = new OkHttpClient();
    String token;
    String currentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSave = findViewById(R.id.btnSave);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        token = SharedPrefManager.getInstance(this).getToken();
        loadUserInfo();

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            String currentPassword = edtCurrentPassword.getText().toString();
            String newPassword = edtNewPassword.getText().toString();
            String confirmPassword = edtConfirmPassword.getText().toString();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            updateUserInfo(name, email, phone);

            if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                if (currentPassword.isEmpty()) {
                    Toast.makeText(this, "Nhập mật khâu hiện tại", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPassword.isEmpty()) {
                    Toast.makeText(this, "Nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                checkCurrentPasswordAndUpdate(currentEmail, currentPassword, newPassword);
            }
        });
    }

    private void loadUserInfo() {
        Request request = new Request.Builder()
                .url(GET_USER_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(body);
                        String name = obj.getString("full_name");
                        currentEmail = obj.getString("email");
                        String phone = obj.optString("phone", "");

                        runOnUiThread(() -> {
                            edtName.setText(name);
                            edtEmail.setText(currentEmail);
                            edtPhone.setText("null".equals(phone) ? "" : phone);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Error parsing user info", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Unauthorized", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateUserInfo(String name, String email, String phone) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject obj = new JSONObject();
        try {
            obj.put("email", email);
            obj.put("fullName", name);
            obj.put("phone", phone);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(obj.toString(), JSON);
        Request request = new Request.Builder()
                .url(UPDATE_PROFILE_URL)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Hồ sơ đã được cập nhật", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void checkCurrentPasswordAndUpdate(String email, String currentPassword, String newPassword) {
        FormBody checkBody = new FormBody.Builder()
                .add("email", email)
                .add("password", currentPassword)
                .build();

        Request checkRequest = new Request.Builder()
                .url(LOGIN_URL)
                .post(checkBody)
                .build();

        client.newCall(checkRequest).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Password check failed", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    updatePassword(email, newPassword);
                } else {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updatePassword(String email, String newPassword) {
        FormBody body = new FormBody.Builder()
                .add("email", email)
                .add("password", newPassword)
                .build();

        Request request = new Request.Builder()
                .url(UPDATE_PASSWORD_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Password update failed", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Mật khẩu đã được cập nhật", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Password update error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.transition.slide_in_left, R.transition.slide_out_right);
    }
}
