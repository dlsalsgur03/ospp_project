package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class LoginRequest {
    @Expose @SerializedName("email") public String email;
    @Expose @SerializedName("password") public String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
