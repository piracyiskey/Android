package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.convenient.R;
import com.example.convenient.Utils.HttpClientInstance;
import com.example.convenient.Utils.SharedPrefManager;
import com.example.convenient.Utils.StringHelper;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button btnLogin;
    CheckBox checkboxRemember;
    SharedPrefManager prefManager;

    private static final String LOGIN_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefManager = new SharedPrefManager(this);

        email = findViewById(R.id.edtEmail);
        password = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        checkboxRemember = findViewById(R.id.checkboxRemember);

        findViewById(R.id.txtForgotPassword).setOnClickListener(v -> {
            startActivity(new Intent(this, RecoveryActivity.class));
            overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.txtRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });

        btnLogin.setOnClickListener(v -> {
            if (validateInput()) {
                login(email.getText().toString().trim(), password.getText().toString());
            }
        });

        loadSavedLogin();
    }

    private void loadSavedLogin() {
        if (prefManager.isRemembered()) {
            email.setText(prefManager.getSavedEmail());
            password.setText(prefManager.getSavedPassword());
            checkboxRemember.setChecked(true);
        }
    }

    private boolean validateInput() {
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString();

        if (mail.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền vào tất cả các ô", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!StringHelper.isValidEmail(mail)) {
            Toast.makeText(this, "Vui lòng nhập email hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void login(String emailStr, String passwordStr) {
        RequestBody formBody = new FormBody.Builder()
                .add("email", emailStr)
                .add("password", passwordStr)
                .build();

        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(formBody)
                .build();

        HttpClientInstance.getInstance().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String resp = response.body().string().trim();
                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject obj = new JSONObject(resp);
                            String token = obj.getString("token");

                            // Save token and optionally email/password
                            prefManager.saveToken(token);
                            if (checkboxRemember.isChecked()) {
                                prefManager.saveLogin(emailStr, passwordStr);
                            } else {
                                prefManager.clearLogin();
                            }

                            startActivity(new Intent(LoginActivity.this, NavActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Lỗi phân tích phản hồi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.transition.slide_in_left, R.transition.no_animation);
    }
}
