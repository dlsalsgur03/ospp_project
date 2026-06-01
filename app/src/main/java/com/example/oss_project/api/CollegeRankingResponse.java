package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CollegeRankingResponse {
    @Expose @SerializedName("rank") public int rank;
    @Expose @SerializedName("college") public String college;
    @Expose @SerializedName("totalSubmissionCount") public int totalSubmissionCount;
    @Expose @SerializedName("userCount") public int userCount;
}
