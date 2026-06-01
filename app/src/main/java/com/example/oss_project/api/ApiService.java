package com.example.oss_project.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/signup")
    Call<ApiResult<Response>> signUp(@Body SignUpRequest request);

    @POST("api/auth/login")
    Call<ApiResult<LoginResponse>> login(@Body LoginRequest request);

    @POST("api/auth/logout")
    Call<ApiResult<Void>> logout(@Header("Authorization") String token);
    @GET("api/users/me")
    Call<ApiResult<UserInfoResponse>> getUserInfo(@Header("Authorization") String token);

    @GET("api/users/me/characters/dex")
    Call<ApiResult<DexData>> getCharacterDex(@Header("Authorization") String token);

    @GET("api/characters/spawns")
    Call<ApiResult<CharacterSpawnListResponse>> getCurrentSpawns();

    @POST("api/submissions")
    Call<ApiResult<Response>> submitCollection(
            @Header("Authorization") String token,
            @Body SubmissionRequest request
    );
}
