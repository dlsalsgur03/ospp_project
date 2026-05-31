package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SignUpRequest {
    @Expose @SerializedName("email") public String email;
    @Expose @SerializedName("password") public String password;
    @Expose @SerializedName("nickname") public String nickname;
    @Expose @SerializedName("college") public String college;
    @Expose @SerializedName("department") public String department;

    public SignUpRequest(String email, String password, String nickname, String college, String department) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.college = college;
        this.department = department;
    }
}
