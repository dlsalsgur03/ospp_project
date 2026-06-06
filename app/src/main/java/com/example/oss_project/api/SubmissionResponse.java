package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubmissionResponse {
    @Expose @SerializedName("submissionId") public Long submissionId;
    @Expose @SerializedName("userId") public Long userId;
    @Expose @SerializedName("sensorId") public Long sensorId;
    @Expose @SerializedName("sensorName") public String sensorName;
    @Expose @SerializedName("rewardExp") public Integer rewardExp;
    @Expose @SerializedName("totalExp") public Integer totalExp;
    @Expose @SerializedName("level") public Integer level;
    @Expose @SerializedName("levelUp") public Boolean levelUp;
    @Expose @SerializedName("totalSubmissionCount") public Integer totalSubmissionCount;
    @Expose @SerializedName("characterCollected") public Boolean characterCollected;
    @Expose @SerializedName("characterReward") public SubmissionCharacterReward characterReward;
    @Expose @SerializedName("nextAvailableAt") public String nextAvailableAt;
    @Expose @SerializedName("createdAt") public String createdAt;
}
