package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubmissionPageResponse {
    @Expose @SerializedName("page") public Integer page;
    @Expose @SerializedName("size") public Integer size;
    @Expose @SerializedName("totalElements") public Long totalElements;
    @Expose @SerializedName("submissions") public List<SubmissionItemResponse> submissions;
}
