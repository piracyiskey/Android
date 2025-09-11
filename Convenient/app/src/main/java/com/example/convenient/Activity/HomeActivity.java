package com.example.convenient.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.convenient.R;
import com.example.convenient.Utils.HttpClientInstance;
import com.example.convenient.Utils.SharedPrefManager;
import com.google.android.gms.auth.api.identity.*;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private static final String LOGIN_URL = "https://nt00o1r8fe.execute-api.ap-southeast-1.amazonaws.com/dev/api/v1/user/google-login";

    private GoogleSignInClient googleSignInClient;
    Button btnGoogle;
    SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefManager = new SharedPrefManager(this);
        btnGoogle = findViewById(R.id.btnGoogle);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });

        findViewById(R.id.txtRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        });
    }


    // Modify your onActivityResult:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Success
            } catch (ApiException e) {
                Log.e("GOOGLE_SIGN_IN", "Sign-in failed code=" + e.getStatusCode(), e);
                Toast.makeText(this, "Google sign-in fail", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGoogleSignIn(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                String fullName = account.getDisplayName();
                String profilePic = (account.getPhotoUrl() != null) ? account.getPhotoUrl().toString() : "";

                loginToServer(email, fullName, profilePic);
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginToServer(String email, String fullName, String profilePic) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("fullName", fullName);
            json.put("profilePic", profilePic);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(body)
                .build();

        HttpClientInstance.getInstance().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String resp = response.body().string().trim();
                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject obj = new JSONObject(resp);
                            String token = obj.getString("token");

                            prefManager.saveToken(token);
                            startActivity(new Intent(HomeActivity.this, LoggedInActivity.class));
                            finish();
                        } else {
                            Toast.makeText(HomeActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(HomeActivity.this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.transition.slide_in_left, R.transition.slide_out_right);
    }
}
