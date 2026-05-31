package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserInfoResponse {
    @Expose @SerializedName("email") public String email;
    @Expose @SerializedName("nickname") public String nickname;
    @Expose @SerializedName("college") public String college;
    @Expose @SerializedName("department") public String department;
    @Expose @SerializedName("level") public int level;
    @Expose @SerializedName("exp") public int exp;

    @Expose @SerializedName("result") public String result;
    @Expose @SerializedName("message") public String message;

    public boolean isSuccess() {
        return "Success".equalsIgnoreCase(result);
    }
}
