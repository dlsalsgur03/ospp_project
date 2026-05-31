package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApiResult<T> {
    @Expose @SerializedName("status") public String status;
    @Expose @SerializedName("message") public String message;
    @Expose @SerializedName("data") public T data; // 실제 알맹이 (LoginResponse 등)
}
