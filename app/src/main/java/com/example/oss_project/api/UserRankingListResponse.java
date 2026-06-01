package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserRankingListResponse {
    @Expose @SerializedName("page") public Integer page;
    @Expose @SerializedName("size") public Integer size;
    @Expose @SerializedName("totalElements") public Long totalElements;
    @Expose @SerializedName("rankings") public List<UserRankingResponse> rankings;
}
