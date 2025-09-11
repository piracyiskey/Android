package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.convenient.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecoveryActivity2 extends AppCompatActivity {

    private static final String TAG = "RecoveryActivity2";
    EditText password, confirm_password;
    Button btnUpdate;

    private static final String UPDATE_PASSWORD_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/update-password";
    private String emailFromPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery2);

        password = findViewById(R.id.edtPassword);
        confirm_password = findViewById(R.id.edtConfirmPassword);
        btnUpdate = findViewById(R.id.btnUpdate);

        // Check if we received the email from previous activity
        if (getIntent().hasExtra(RecoveryActivity.EXTRA_EMAIL)) {
            emailFromPrevious = getIntent().getStringExtra(RecoveryActivity.EXTRA_EMAIL);
            Log.d(TAG, "Received email: " + emailFromPrevious);
        } else {
            Log.e(TAG, "No email received from previous activity");
            showToast("Error: No email provided");
            finish();
            return;
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        btnUpdate.setOnClickListener(v -> {
            if (validateInput()) {
                updatePassword(emailFromPrevious, password.getText().toString());
            }
        });

        Log.d(TAG, "RecoveryActivity2 started successfully");
    }

    private boolean validateInput() {
        String pass = password.getText().toString();
        String confirmPass = confirm_password.getText().toString();

        if (pass.isEmpty() || confirmPass.isEmpty()) {
            showToast("Vui lòng điền vào tất cả các ô");
            return false;
        }

        if (!pass.equals(confirmPass)) {
            showToast("Mật khẩu không khớp");
            return false;
        }

        return true;
    }

    private void updatePassword(String email, String newPassword) {
        Log.d(TAG, "Updating password for email: " + email);
        OkHttpClient client = new OkHttpClient();

        FormBody body = new FormBody.Builder()
                .add("email", email)
                .add("password", newPassword)
                .build();

        Request request = new Request.Builder()
                .url(UPDATE_PASSWORD_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network failure: " + e.getMessage());
                runOnUiThread(() -> showToast("Lỗi kết nối server"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "Server response: " + responseBody);

                runOnUiThread(() -> {
                    if (responseBody.equalsIgnoreCase("updated")) {
                        Log.d(TAG, "Password updated successfully");
                        showToast("Cập nhật mật khẩu thành công");
                        Intent intent = new Intent(RecoveryActivity2.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "Password update failed");
                        showToast("Cập nhật thất bại");
                    }
                });
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(RecoveryActivity2.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back button pressed");
        super.onBackPressed();
    }

    @Override
    public void finish() {
        Log.d(TAG, "Activity finishing");
        super.finish();
        overridePendingTransition(R.transition.slide_in_left, R.transition.slide_out_right);
    }
}