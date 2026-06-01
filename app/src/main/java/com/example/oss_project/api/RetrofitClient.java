package com.example.oss_project.api;

import com.example.oss_project.BleDeviceData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://bunkmate-dreamily-anemia.ngrok-free.dev/";
    private static Retrofit retrofit = null;
    private static final String API_KEY = "" ;

    public static Retrofit getClient() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static postdata makePostData(BleDeviceData ble, double lat, double lon, String sender) {
        return new postdata(
                API_KEY,      // key
                "team 1",
                ble.deviceName,       // sensor
                ble.deviceAddress,  // mac
                ble.temp,
                ble.humidity,
                ble.aqi,
                ble.tvoc,
                ble.eco2,
                ble.timestampValue,
                lat,                // 위도
                lon,                // 경도
                sender,
                ble.rssi// 스마트폰 UUID
        );
    }
}
