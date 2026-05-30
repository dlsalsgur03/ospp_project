package com.example.oss_project.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/signup")
    Call<Response> signUp(@Body SignUpRequest request);
}
