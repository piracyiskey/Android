package com.example.convenient.Utils;

import okhttp3.OkHttpClient;

public class HttpClientInstance {
    private static OkHttpClient instance;

    public static OkHttpClient getInstance() {
        if (instance == null) {
            instance = new OkHttpClient.Builder()
                    .cookieJar(new MyCookieJar())
                    .build();
        }
        return instance;
    }
}
