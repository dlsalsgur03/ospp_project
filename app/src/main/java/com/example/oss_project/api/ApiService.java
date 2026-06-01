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

    @GET("api/rankings/me")
    Call<ApiResult<MyRankingResponse>> getPersonalRanking(@Header("Authorization") String token);

    @GET("api/rankings/users")
    Call<ApiResult<UserRankingListResponse>> getUsersRanking(@Header("Authorization") String token);

    @GET("api/rankings/colleges")
    Call<ApiResult<CollegeRankingListResponse>> getCollegeRanking(@Header("Authorization") String token);

    @GET("api/rankings/departments")
    Call<ApiResult<DepartmentRankingListResponse>> getDepartmentRanking(@Header("Authorization") String token);

    @GET("api/users/me")
    Call<ApiResult<UserInfoResponse>> getUserInfo(@Header("Authorization") String token);

    @GET("api/users/me/characters/dex")
    Call<ApiResult<DexData>> getCharacterDex(@Header("Authorization") String token);
}
