package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.convenient.R;
import com.example.convenient.Utils.HttpClientInstance;
import com.example.convenient.Utils.StringHelper;
import com.example.convenient.Utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText full_name, email, password, confirm_password;
    Button btnRegister;

    private static final String REGISTER_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/register";
    private static final String LOGIN_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        full_name = findViewById(R.id.edtFullName);
        email = findViewById(R.id.edtEmail);
        password = findViewById(R.id.edtPassword);
        confirm_password = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.txtLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });

        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                processRegister();
            }
        });
    }

    private boolean validateInput() {
        String name = full_name.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString();
        String confirmPass = confirm_password.getText().toString();

        if (name.isEmpty() || mail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền vào tất cả các ô", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!StringHelper.isValidEmail(mail)) {
            Toast.makeText(this, "Vui lòng nhập email hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void processRegister() {
        String name = full_name.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString();

        RequestBody formBody = new FormBody.Builder()
                .add("full_name", name)
                .add("email", mail)
                .add("password", pass)
                .build();

        Request request = new Request.Builder()
                .url(REGISTER_URL)
                .post(formBody)
                .build();

        HttpClientInstance.getInstance().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String res = response.body().string().trim();
                if (res.equalsIgnoreCase("success")) {
                    // Automatically log in the user
                    loginAfterRegister(mail, pass);
                } else if (res.equalsIgnoreCase("used")) {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void loginAfterRegister(String email, String password) {
        RequestBody loginBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();

        Request loginRequest = new Request.Builder()
                .url(LOGIN_URL)
                .post(loginBody)
                .build();

        HttpClientInstance.getInstance().newCall(loginRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Lỗi khi đăng nhập tự động", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resBody = response.body().string().trim();
                    try {
                        JSONObject json = new JSONObject(resBody);
                        String token = json.optString("token", null);
                        if (token != null) {
                            SharedPrefManager.getInstance(RegisterActivity.this).saveToken(token);
                            runOnUiThread(() -> {
                                Toast.makeText(RegisterActivity.this, "Đăng ký và đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, NavActivity.class));
                                finish();
                            });
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(RegisterActivity.this, "Token không hợp lệ", Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(RegisterActivity.this, "Lỗi khi phân tích phản hồi", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công, nhưng đăng nhập thất bại", Toast.LENGTH_SHORT).show());
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
