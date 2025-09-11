package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.convenient.R;
import com.example.convenient.Utils.StringHelper;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecoveryActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";
    private static final String CHECK_EMAIL_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/check-email";
    private static final String SEND_OTP_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/send-otp";

    private EditText edtEmail;
    private Button btnNext;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);

        edtEmail = findViewById(R.id.edtEmail);
        btnNext = findViewById(R.id.btnNext);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        btnNext.setOnClickListener(v -> {
            String emailInput = edtEmail.getText().toString().trim();
            if (validateInput()) {
                checkEmailAndProceed(emailInput);
            }
        });
    }

    private boolean validateInput() {
        String mail = edtEmail.getText().toString().trim();

        if (mail.isEmpty()) {
            showToast("Vui nhập email đã đăng ký");
            return false;
        }

        if (!StringHelper.isValidEmail(mail)) {
            showToast("Vui lòng nhập email hợp lệ");
            return false;
        }

        return true;
    }


    private void checkEmailAndProceed(String emailInput) {
        FormBody body = new FormBody.Builder()
                .add("email", emailInput)
                .build();

        Request request = new Request.Builder()
                .url(CHECK_EMAIL_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showToast("Lỗi kết nối"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if (result.equalsIgnoreCase("exists")) {
                    runOnUiThread(() -> {
                        navigateToVerify(emailInput);
                        sendOtpInBackground(emailInput);
                    });
                } else {
                    runOnUiThread(() -> showToast("Email không tồn tại"));
                }
            }
        });
    }

    private void navigateToVerify(String emailInput) {
        Intent intent = new Intent(RecoveryActivity.this, VerifyActivity.class);
        intent.putExtra(EXTRA_EMAIL, emailInput);
        startActivity(intent);
        overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
    }

    private void sendOtpInBackground(String emailInput) {
        FormBody body = new FormBody.Builder()
                .add("email", emailInput)
                .build();

        Request request = new Request.Builder()
                .url(SEND_OTP_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showToast("Lỗi gửi OTP"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if (!result.equalsIgnoreCase("otp_sent")) {
                    runOnUiThread(() -> showToast("Không gửi được OTP"));
                }
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(RecoveryActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
