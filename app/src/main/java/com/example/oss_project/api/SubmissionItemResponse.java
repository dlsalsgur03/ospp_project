package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubmissionItemResponse {
    @Expose @SerializedName("submissionId") public Long submissionId;
    @Expose @SerializedName("sensorId") public Long sensorId;
    @Expose @SerializedName("sensorName") public String sensorName;
    @Expose @SerializedName("locationName") public String locationName;
    @Expose @SerializedName("temperature") public Double temperature;
    @Expose @SerializedName("humidity") public Double humidity;
    @Expose @SerializedName("eco2") public Integer eco2;
    @Expose @SerializedName("airQuality") public Integer airQuality;
    @Expose @SerializedName("rssi") public Integer rssi;
    @Expose @SerializedName("rewardExp") public Integer rewardExp;
    @Expose @SerializedName("measuredAt") public String measuredAt;
    @Expose @SerializedName("submittedAt") public String submittedAt;
}
