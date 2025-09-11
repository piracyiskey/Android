package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.convenient.R;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VerifyActivity extends AppCompatActivity {

    private static final String VERIFY_OTP_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/verify-otp";
    private static final String RESEND_OTP_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/send-otp";

    private EditText[] otpBoxes = new EditText[6];
    private Button btnNext;
    private TextView txtAgain;
    private OkHttpClient client = new OkHttpClient();

    private String emailFromPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        emailFromPrevious = getIntent().getStringExtra(RecoveryActivity.EXTRA_EMAIL);

        otpBoxes[0] = findViewById(R.id.otp1);
        otpBoxes[1] = findViewById(R.id.otp2);
        otpBoxes[2] = findViewById(R.id.otp3);
        otpBoxes[3] = findViewById(R.id.otp4);
        otpBoxes[4] = findViewById(R.id.otp5);
        otpBoxes[5] = findViewById(R.id.otp6);

        setupOtpInputs();
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        btnNext = findViewById(R.id.btnNext);
        txtAgain = findViewById(R.id.txtAgain);

        btnNext.setOnClickListener(v -> {
            String otp = getOtpInput();
            if (otp.length() == 6) {
                verifyOtp(emailFromPrevious, otp);
            } else {
                showToast("Nhập đủ 6 chữ số OTP");
            }
        });

        txtAgain.setOnClickListener(v -> resendOtp(emailFromPrevious));
    }

    private void setupOtpInputs() {
        for (int i = 0; i < otpBoxes.length; i++) {
            final int index = i;

            otpBoxes[index].addTextChangedListener(new TextWatcher() {
                private boolean isPasting = false;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    isPasting = after > 1;  // Detect paste
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = s.toString();

                    if (isPasting && text.length() == 6) {
                        // Handle full OTP paste
                        for (int j = 0; j < 6; j++) {
                            otpBoxes[j].setText(String.valueOf(text.charAt(j)));
                        }
                        otpBoxes[5].requestFocus();
                    } else if (text.length() == 1 && index < otpBoxes.length - 1) {
                        otpBoxes[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpBoxes[index].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == android.view.KeyEvent.ACTION_DOWN &&
                        keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                        otpBoxes[index].getText().toString().isEmpty() &&
                        index > 0) {
                    otpBoxes[index - 1].setText("");
                    otpBoxes[index - 1].requestFocus();
                    return true;
                }
                return false;
            });
        }
    }

    private String getOtpInput() {
        StringBuilder sb = new StringBuilder();
        for (EditText box : otpBoxes) {
            sb.append(box.getText().toString());
        }
        return sb.toString();
    }

    private void verifyOtp(String email, String otp) {
        FormBody body = new FormBody.Builder()
                .add("email", email)
                .add("otp", otp)
                .build();

        Request request = new Request.Builder()
                .url(VERIFY_OTP_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showToast("Lỗi kết nối"));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if (result.equalsIgnoreCase("otp_valid")) {
                    Intent intent = new Intent(VerifyActivity.this, RecoveryActivity2.class);
                    intent.putExtra(RecoveryActivity.EXTRA_EMAIL, email);
                    startActivity(intent);
                    finish();
                } else {
                    runOnUiThread(() -> showToast("OTP không hợp lệ"));
                }
            }
        });
    }

    private void resendOtp(String email) {
        FormBody body = new FormBody.Builder()
                .add("email", email)
                .build();

        Request request = new Request.Builder()
                .url(RESEND_OTP_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showToast("Không gửi lại được OTP"));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                if (result.equalsIgnoreCase("otp_sent")) {
                    runOnUiThread(() -> showToast("OTP đã được gửi lại"));
                } else {
                    runOnUiThread(() -> showToast("Thất bại khi gửi lại OTP"));
                }
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(VerifyActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
