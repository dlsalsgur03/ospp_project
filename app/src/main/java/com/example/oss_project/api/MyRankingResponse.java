package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyRankingResponse {
    @Expose @SerializedName("overallRank") public Integer overallRank;
    @Expose @SerializedName("collegeRank") public Integer collegeRank;
    @Expose @SerializedName("departmentRank") public Integer departmentRank;
    @Expose @SerializedName("totalSubmissionCount") public Integer totalSubmissionCount;
}
