package com.example.stationeryshopapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    // ===== CHANGE THIS URL BASED ON YOUR SETUP =====
    //
    // LOCAL (same PC as emulator):
    //   "http://10.0.2.2:8080/"
    //
    // LOCAL (phone on same Wi-Fi):
    //   "http://YOUR_PC_IP:8080/"
    //
    // NGROK (temporary tunnel):
    //   "https://your-tunnel.ngrok-free.dev/"
    //
    // PRODUCTION (Railway/Render - use this for Play Store release):
    //   "https://studygrid-backend-production.up.railway.app/"
    //
    private static final String BASE_URL = "https://professor-thrive-family.ngrok-free.dev/";

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(ApiService.class);
    }
}
