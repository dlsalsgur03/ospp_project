package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubmissionRequest {
    @Expose
    @SerializedName("sensorId")
    public long sensorId;

    @Expose
    @SerializedName("temperature")
    public double temperature;

    @Expose
    @SerializedName("humidity")
    public double humidity;

    @Expose
    @SerializedName("eco2")
    public int eco2;

    @Expose
    @SerializedName("airQuality")
    public int airQuality;

    @Expose
    @SerializedName("rssi")
    public int rssi;

    @Expose
    @SerializedName("latitude")
    public double latitude;

    @Expose
    @SerializedName("longitude")
    public double longitude;

    @Expose
    @SerializedName("measuredAt")
    public String measuredAt;

    public SubmissionRequest(long sensorId, double temperature, double humidity, int eco2,
                             int airQuality, int rssi, double latitude, double longitude,
                             String measuredAt) {
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
