package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubmissionRequest {

    @Expose
    @SerializedName("sensorId")
    private Long sensorId;

    @Expose
    @SerializedName("temperature")
    private float temperature;

    @Expose
    @SerializedName("humidity")
    private float humidity;

    @Expose
    @SerializedName("eco2")
    private int eco2;

    @Expose
    @SerializedName("airQuality")
    private int airQuality;

    @Expose
    @SerializedName("rssi")
    private int rssi;

    @Expose
    @SerializedName("latitude")
    private double latitude;

    @Expose
    @SerializedName("longitude")
    private double longitude;

    @Expose
    @SerializedName("measuredAt")
    private String measuredAt;

    public SubmissionRequest(
            Long sensorId,
            float temperature,
            float humidity,
            int eco2,
            int airQuality,
            int rssi,
            double latitude,
            double longitude,
            String measuredAt
    ) {
        this.sensorId = sensorId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.eco2 = eco2;
        this.airQuality = airQuality;
        this.rssi = rssi;
        this.latitude = latitude;
        this.longitude = longitude;
        this.measuredAt = measuredAt;
    }
}