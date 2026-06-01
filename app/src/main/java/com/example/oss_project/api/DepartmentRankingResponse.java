package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DepartmentRankingResponse {
    @Expose @SerializedName("rank") public int rank;
    @Expose @SerializedName("department") public String department;
    @Expose @SerializedName("totalSubmissionCount") public int totalSubmissionCount;
    @Expose @SerializedName("userCount") public int userCount;
}
