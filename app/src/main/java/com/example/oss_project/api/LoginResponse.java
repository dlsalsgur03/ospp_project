package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @Expose @SerializedName("result") public String result;
    @Expose @SerializedName("message") public String message;
    @Expose @SerializedName("token") public String token;

    public boolean isSuccess() {
        return "Success".equalsIgnoreCase(result);
    }
}
