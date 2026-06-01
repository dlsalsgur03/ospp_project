package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubmissionRequest {
    @Expose @SerializedName("sensorId")     private Long sensorId;
    @Expose @SerializedName("temperature")  private float temperature;
    @Expose @SerializedName("humidity")     private float humidity;
    @Expose @SerializedName("eco2")         private int eco2;
    @Expose @SerializedName("airQuality")   private int airQuality; // AQI
    @Expose @SerializedName("rssi")         private int rssi;
    @Expose @SerializedName("lat")          private double lat;
    @Expose @SerializedName("lon")          private double lon;
    @Expose @SerializedName("measuredAt")   private long measuredAt; // 측정 시간 (timestamp)

    public SubmissionRequest(Long sensorId, float temperature, float humidity, int eco2, int airQuality, int rssi, double lat, double lon, long measuredAt) {
        this.sensorId = sensorId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.eco2 = eco2;
        this.airQuality = airQuality;
        this.rssi = rssi;
        this.lat = lat;
        this.lon = lon;
        this.measuredAt = measuredAt;
    }
}
