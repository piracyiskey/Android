package com.example.convenient.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "convenient_app_prefs";
    private static final String KEY_FIRST_TIME = "isFirstTimeLaunch";

    private static final String KEY_REMEMBER = "remember_me";
    private static final String KEY_EMAIL = "saved_email";
    private static final String KEY_PASSWORD = "saved_password";

    private static final String KEY_TOKEN = "jwt_token";

    private static SharedPrefManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // First-time launch
    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(KEY_FIRST_TIME, true);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(KEY_FIRST_TIME, isFirstTime);
        editor.apply();
    }

    // Remember Me
    public void saveLogin(String email, String password) {
        editor.putBoolean(KEY_REMEMBER, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public void clearLogin() {
        editor.remove(KEY_REMEMBER);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.apply();
    }

    public boolean isRemembered() {
        return pref.getBoolean(KEY_REMEMBER, false);
    }

    public String getSavedEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getSavedPassword() {
        return pref.getString(KEY_PASSWORD, "");
    }

    // JWT Token
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
}
