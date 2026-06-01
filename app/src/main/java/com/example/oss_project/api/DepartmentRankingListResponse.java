package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DepartmentRankingListResponse {
    @Expose @SerializedName("rankings") public List<DepartmentRankingResponse> rankings;
}
