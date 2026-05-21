package com.example.oss_project.api;

import android.util.Log;

import com.example.oss_project.BleDeviceData;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class postdata {

    @Expose @SerializedName("key")       private String key;
    @Expose @SerializedName("team")      private String team;
    @Expose @SerializedName("sensor")    private String sensor;
    @Expose @SerializedName("mac")       private String mac;
    @Expose @SerializedName("temp")      private float temp;
    @Expose @SerializedName("humidity")  private float humidity;
    @Expose @SerializedName("AQI")       private int AQI;
    @Expose @SerializedName("TVOC")      private int TVOC;
    @Expose @SerializedName("eCO2")      private int eCO2;
    @Expose @SerializedName("timestamp") private long timestamp;
    @Expose @SerializedName("lat")       private double lat;
    @Expose @SerializedName("lon")       private double lon;
    @Expose @SerializedName("sender")    private String sender;
    @Expose @SerializedName("rssi") private int rssi;


    public postdata(String key, String team, String sensor, String mac,
                    float temp, float humidity, int AQI, int TVOC, int eCO2, long timestamp, double lat, double lon, String sender, int rssi) {
        this.key = key;
        this.team = team;
        this.sensor = sensor;
        this.mac = mac;
        this.temp = temp;
        this.humidity = humidity;
        this.AQI = AQI;
        this.TVOC = TVOC;
        this.eCO2 = eCO2;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lon = lon;
        this.sender = sender;
        this.rssi = rssi;
    }
    public void data_show() {
        Log.e("test", key + " " + team + " " + sensor + " " + mac +
                " temp:" + temp + " humidity:" + humidity +
                " AQI:" + AQI + " TVOC:" + TVOC + " eCO2:" + eCO2 +
                " timestamp:" + timestamp + " lat:" + lat + " lon:" + lon +
                " sender:" + sender + "rssi:" + rssi);
    }

    public float getTemp()      { return temp; }
    public float getHumidity()  { return humidity; }
    public int getAQI()         { return AQI; }
    public int getTVOC()        { return TVOC; }
    public int getECO2()        { return eCO2; }
    public long getTimestamp()  { return timestamp; }
}