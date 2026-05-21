package com.example.oss_project.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface comm_data {
    @POST("sensor/opensrc/upload/")
    Call<Response> post_json(@Body postdata pd);
}


 /*
    @FormUrlEncoded
    @POST("sensor/opnsrc/test/")
    Call<String> post(
        @Field("user") String user,
        @Field("data") String data
    );
    */

    /*
    @GET("sensor/opnsrc/test/")
    Call<String> get(
            @Query("user") String user,
            @Query("data") String data
    );
     */
