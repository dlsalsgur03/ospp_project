package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserRankingResponse {
    @Expose @SerializedName("rank") public int rank;
    @Expose @SerializedName("userId") public Long userId;
    @Expose @SerializedName("nickname") public String nickname;
    @Expose @SerializedName("college") public String college;
    @Expose @SerializedName("department") public String department;
    @Expose @SerializedName("level") public int level;
    @Expose @SerializedName("exp") public int exp;
    @Expose @SerializedName("totalSubmissionCount") public Integer totalSubmissionCount;
}
