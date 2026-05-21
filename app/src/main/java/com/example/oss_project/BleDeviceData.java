package com.example.oss_project;

public class BleDeviceData {
    public String deviceAddress;
    public String deviceName;
    public String serviceUuid;
    public String rawHex;
    public byte[] payload;
    public int rssi;

    public float temp;
    public float humidity;
    public int aqi;
    public int tvoc;
    public int eco2;
    public long timestampValue;

    public long receivedAt;
    public boolean savedToCsv;

    // 추가
    public Integer txPower;     // 없으면 null
    public Integer pathLoss;    // 없으면 null
    public boolean savedToSignalCsv;
    public long timeDiffSec;
    public boolean isTimeValid;

    public BleDeviceData(String deviceAddress, String deviceName, String serviceUuid,
                         String rawHex, byte[] payload, int rssi,
                         float temp, float humidity, int aqi, int tvoc, int eco2,
                         long timestampValue, long receivedAt,
                         Integer txPower, Integer pathLoss, long timeDiffSec,
                         boolean isTimeValid){
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        this.serviceUuid = serviceUuid;
        this.rawHex = rawHex;
        this.payload = payload;
        this.rssi = rssi;
        this.temp = temp;
        this.humidity = humidity;
        this.aqi = aqi;
        this.tvoc = tvoc;
        this.eco2 = eco2;
        this.timestampValue = timestampValue;
        this.receivedAt = receivedAt;
        this.savedToCsv = false;

        this.txPower = txPower;
        this.pathLoss = pathLoss;
        this.savedToSignalCsv = false;
        this.timeDiffSec = timeDiffSec;
        this.isTimeValid = isTimeValid;

    }
}