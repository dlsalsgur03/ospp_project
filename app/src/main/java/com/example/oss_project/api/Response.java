package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response {
    @Expose
    @SerializedName("result")
    public String result;

    @Expose
    @SerializedName("message")
    public String message;

    @Expose
    @SerializedName("received_data")
    public ReceivedData receivedData;

    public static class ReceivedData {
        @Expose
        @SerializedName("team")
        public String team;

        @Expose
        @SerializedName("sensor")
        public String sensor;
    }

    public boolean isSuccess() {
        return "Success".equalsIgnoreCase(result);
    }
}
