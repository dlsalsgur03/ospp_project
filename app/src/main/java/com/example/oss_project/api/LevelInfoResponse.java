package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LevelInfoResponse {
    @Expose @SerializedName("userId") public Long userId;
    @Expose @SerializedName("level") public Integer level;
    @Expose @SerializedName("currentExp") public Integer currentExp;
    @Expose @SerializedName("currentLevelMinExp") public Integer currentLevelMinExp;
    @Expose @SerializedName("nextLevelExp") public Integer nextLevelExp;
    @Expose @SerializedName("requiredExpToNextLevel") public Integer requiredExpToNextLevel;
    @Expose @SerializedName("progressRate") public Double progressRate;
}
