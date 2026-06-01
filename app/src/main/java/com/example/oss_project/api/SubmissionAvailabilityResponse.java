package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubmissionAvailabilityResponse {
    @Expose @SerializedName("sensorId") public Long sensorId;
    @Expose @SerializedName("available") public boolean available;
    @Expose @SerializedName("currentTimeSlot") public String currentTimeSlot;
    @Expose @SerializedName("alreadySubmitted") public boolean alreadySubmitted;
    @Expose @SerializedName("nextAvailableAt") public String nextAvailableAt;
}
