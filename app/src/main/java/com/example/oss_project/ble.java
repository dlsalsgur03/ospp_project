package com.example.oss_project;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ble {
    private MainActivity mainActivity;
    private BluetoothLeScanner bluetoothLeScanner;

    // 최신 상태 저장용: 같은 장치 + 같은 UUID는 최신값으로 갱신
    private final Map<String, BleDeviceData> scannedDataMap = new HashMap<>();

    // 누적 저장용: 들어온 모든 기록 저장
    private final List<BleDeviceData> scanHistoryList = new ArrayList<>();

    // UI 필터 상태
    private String selectedUuidFilter = null;   // null 또는 ""이면 전체
    private String nameKeywordFilter = "";
    private Integer minRssiFilter = null;

    public void bleInitialize(MainActivity activity) {
        this.mainActivity = activity;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            this.bluetoothLeScanner = adapter.getBluetoothLeScanner();
        }
    }

    public void startScan() {
        if (bluetoothLeScanner == null) return;

        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        List<ScanFilter> filters = new ArrayList<>();
        bluetoothLeScanner.startScan(filters, scanSettings, scanCallback);

        Log.d("BLE_SCAN", "BLE 전체 스캔 시작");
    }

    public void stopScan() {
        if (bluetoothLeScanner != null
                && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner.stopScan(scanCallback);
            Log.d("BLE_SCAN", "BLE 스캔 중지");
        }
    }

    public void clearScannedData() {
        scannedDataMap.clear();
        scanHistoryList.clear();
        notifyFilteredDataToUi();
    }

    public void setUuidFilter(String uuid) {
        this.selectedUuidFilter = uuid;
        notifyFilteredDataToUi();
    }

    public void setNameKeywordFilter(String keyword) {
        this.nameKeywordFilter = keyword != null ? keyword.trim() : "";
        notifyFilteredDataToUi();
    }

    public void setMinRssiFilter(Integer minRssi) {
        this.minRssiFilter = minRssi;
        notifyFilteredDataToUi();
    }

    public List<String> getDiscoveredUuids() {
        List<String> result = new ArrayList<>();
        for (BleDeviceData data : scannedDataMap.values()) {
            if (data.serviceUuid != null && !result.contains(data.serviceUuid)) {
                result.add(data.serviceUuid);
            }
        }
        return result;
    }

    private List<BleDeviceData> getFilteredDataList() {
        List<BleDeviceData> filteredList = new ArrayList<>();

        for (BleDeviceData data : scannedDataMap.values()) {
            if (matchesFilter(data)) {
                filteredList.add(data);
            }
        }

        return filteredList;
    }

    private boolean matchesFilter(BleDeviceData data) {
        if (data == null) return false;

        if (selectedUuidFilter != null && !selectedUuidFilter.isEmpty()) {
            if (!selectedUuidFilter.equalsIgnoreCase(data.serviceUuid)) {
                return false;
            }
        }

        if (nameKeywordFilter != null && !nameKeywordFilter.isEmpty()) {
            String keyword = nameKeywordFilter.toLowerCase();
            String deviceName = data.deviceName != null ? data.deviceName.toLowerCase() : "";
            if (!deviceName.contains(keyword)) {
                return false;
            }
        }

        if (minRssiFilter != null && data.rssi < minRssiFilter) {
            return false;
        }

        return true;
    }

    private void notifyFilteredDataToUi() {
        List<BleDeviceData> filteredList = getFilteredDataList();
        mainActivity.runOnUiThread(() -> {
            mainActivity.onFilteredBleDataUpdated(filteredList, getDiscoveredUuids()); // 이 줄 추가!
        });
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device == null) return;

            if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            String deviceAddress = device.getAddress() != null
                    ? device.getAddress().toUpperCase()
                    : "UNKNOWN";

            int rssi = result.getRssi();

            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord == null) return;

            Integer txPower = null;
            Integer pathloss = null;

            int txPowerLevel = scanRecord.getTxPowerLevel();

            if(txPowerLevel != Integer.MIN_VALUE) {
                txPower = txPowerLevel;
                pathloss = txPower - rssi;
            }

            String name = "Unknown";

// 1순위: 광고 패킷 안 이름
            String advName = scanRecord.getDeviceName();
            if (advName != null && !advName.trim().isEmpty()) {
                name = advName;
            }

// 2순위: 시스템이 알고 있는 이름
            if ("Unknown".equals(name)) {
                String deviceName = device.getName();
                if (deviceName != null && !deviceName.trim().isEmpty()) {
                    name = deviceName;
                }
            }

            Map<ParcelUuid, byte[]> serviceDataMap = scanRecord.getServiceData();
            if (serviceDataMap == null || serviceDataMap.isEmpty()) return;

            for (Map.Entry<ParcelUuid, byte[]> entry : serviceDataMap.entrySet()) {
                ParcelUuid uuid = entry.getKey();
                byte[] serviceData = entry.getValue();

                if (uuid == null || serviceData == null) continue;

                String serviceUuid = uuid.toString();
                String rawHex = bytesToHex(serviceData);

                Log.d("BLE_UUID", "UUID = " + serviceUuid);
                Log.d("BLE_RAW", "RAW = " + Arrays.toString(serviceData));
                Log.d("BLE_HEX", "HEX = " + rawHex);

                float temp = 0.0f;
                float humidity = 0.0f;
                int aqi = 0;
                int tvoc = 0;
                int eco2 = 0;
                long payloadTimestamp = 0L;
                long timeDiffSec = 0L;
                boolean isTimeValid = false;


                if (serviceData.length >= 13) {
                    short tempRaw = (short) (((serviceData[1] & 0xFF) << 8) | (serviceData[0] & 0xFF));
                    int humidityRaw = ((serviceData[3] & 0xFF) << 8) | (serviceData[2] & 0xFF);
                    aqi = serviceData[4] & 0xFF;
                    tvoc = ((serviceData[6] & 0xFF) << 8) | (serviceData[5] & 0xFF);
                    eco2 = ((serviceData[8] & 0xFF) << 8) | (serviceData[7] & 0xFF);

                    payloadTimestamp =
                            ((long) (serviceData[12] & 0xFF) << 24) |
                                    ((long) (serviceData[11] & 0xFF) << 16) |
                                    ((long) (serviceData[10] & 0xFF) << 8) |
                                    ((long) (serviceData[9] & 0xFF));

                    temp = tempRaw / 100.0f;
                    humidity = humidityRaw / 100.0f;

                    long receivedAt = System.currentTimeMillis();
                    timeDiffSec = (receivedAt/1000L) - payloadTimestamp;
                    long maxAllowedDiffSec = 60;
                    isTimeValid = Math.abs(timeDiffSec) <= maxAllowedDiffSec;

                    Log.d("BLE_PARSE",
                            "temp=" + temp +
                                    ", humidity=" + humidity +
                                    ", aqi=" + aqi +
                                    ", tvoc=" + tvoc +
                                    ", eco2=" + eco2 +
                                    ", ts=" + payloadTimestamp);
                } else {
                    Log.w("BLE_PARSE", "serviceData 길이 부족: " + serviceData.length + " bytes");
                }

                BleDeviceData bleData = new BleDeviceData(
                        deviceAddress,
                        name,
                        serviceUuid,
                        rawHex,
                        serviceData,
                        rssi,
                        temp,
                        humidity,
                        aqi,
                        tvoc,
                        eco2,
                        payloadTimestamp,
                        System.currentTimeMillis(),
                        txPower,
                        pathloss,
                        timeDiffSec,
                        isTimeValid
                );

                String key = deviceAddress + "_" + serviceUuid;

                // 최신 상태 저장
                scannedDataMap.put(key, bleData);

                // 누적 이력 저장
                scanHistoryList.add(bleData);
            }

            notifyFilteredDataToUi();
        }
    };

    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format(Locale.getDefault(), "%02X ", b));
        }
        return sb.toString().trim();
    }

    private String getServiceNameFromUuid(String uuid) {
        if (uuid == null) return "Unknown_Service";

        if (uuid.equalsIgnoreCase("0000181a-0000-1000-8000-00805f9b34fb")) {
            return "Environmental_Sensing";
        } else if (uuid.equalsIgnoreCase("12345678-1234-5678-1234-56789abcdef0")) {
            return "Custom_Sensor_Service";
        }

        return "Unknown_Service";
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.trim().isEmpty()) return "Unknown_Service";
        return name.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    // 최신 필터 결과 저장: 현재 화면의 최신값들
    public void saveFilteredDataToCsv() {
        List<BleDeviceData> filteredList = getFilteredDataList();

        if (filteredList.isEmpty()) {
            Log.d("BLE_CSV", "저장할 필터링 데이터가 없음");
            return;
        }

        // UUID 필터가 없으면 파일명과 내용이 섞일 수 있어서 저장 막음
        String uuidForFile = selectedUuidFilter;
        if (uuidForFile == null || uuidForFile.trim().isEmpty()) {
            Log.d("BLE_CSV", "UUID 필터를 먼저 선택해야 저장 가능");
            return;
        }

        List<BleDeviceData> unsavedLatest = new ArrayList<>();
        for (BleDeviceData data : filteredList) {
            if (!data.savedToCsv) {
                unsavedLatest.add(data);
            }
        }

        if (unsavedLatest.isEmpty()) {
            Log.d("BLE_CSV", "새로 저장할 최신 데이터가 없음");
            return;
        }

        saveListToCsv(unsavedLatest, uuidForFile);
    }

    // 누적 이력 저장: 아직 저장 안 된 것만 저장
    public void saveFilteredHistoryToCsv() {
        String uuidForFile = selectedUuidFilter;

        // UUID 필터가 없으면 서비스별 파일 분리가 애매하니까 저장 막음
        if (uuidForFile == null || uuidForFile.trim().isEmpty()) {
            Log.d("BLE_CSV", "UUID 필터를 먼저 선택해야 저장 가능");
            return;
        }

        List<BleDeviceData> unsavedFilteredHistory = new ArrayList<>();

        for (BleDeviceData data : scanHistoryList) {
            if (matchesFilter(data) && !data.savedToCsv) {
                unsavedFilteredHistory.add(data);
            }
        }

        if (unsavedFilteredHistory.isEmpty()) {
            Log.d("BLE_CSV", "새로 저장할 누적 필터 데이터가 없음");
            return;
        }

        saveListToCsv(unsavedFilteredHistory, uuidForFile);
    }

    private void saveListToCsv(List<BleDeviceData> dataList, String uuidForFile) {
        String serviceName = getServiceNameFromUuid(uuidForFile);
        String safeServiceName = sanitizeFileName(serviceName);

        File dir = mainActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null) {
            Log.e("BLE_CSV", "저장 폴더를 가져올 수 없음");
            return;
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File csvFile = new File(dir, safeServiceName + ".csv");
        boolean fileExists = csvFile.exists();

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            if (!fileExists) {
                writer.append("received_at,device_name,device_address,service_uuid,service_name,rssi,temp,humidity,aqi,tvoc,eco2,payload_timestamp,time_diff_sec,isTimeValid,raw_hex\n");
            }

            for (BleDeviceData data : dataList) {
                // 혹시 다른 UUID가 섞여 있으면 저장하지 않음
                if (data.serviceUuid == null || !data.serviceUuid.equalsIgnoreCase(uuidForFile)) {
                    continue;
                }

                String receivedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date(data.receivedAt));

                String row = String.format(Locale.getDefault(),
                        "%s,%s,%s,%s,%s,%d,%.2f,%.2f,%d,%d,%d,%d,%d,%s,%s\n",
                        escapeCsv(receivedAt),
                        escapeCsv(data.deviceName),
                        escapeCsv(data.deviceAddress),
                        escapeCsv(data.serviceUuid),
                        escapeCsv(getServiceNameFromUuid(data.serviceUuid)),
                        data.rssi,
                        data.temp,
                        data.humidity,
                        data.aqi,
                        data.tvoc,
                        data.eco2,
                        data.timestampValue,
                        data.timeDiffSec,
                        String.valueOf(data.isTimeValid),
                        escapeCsv(data.rawHex)
                );

                writer.append(row);

                // 저장 성공한 항목은 다시 저장되지 않도록 표시
                data.savedToCsv = true;
            }

            writer.flush();
            Log.d("BLE_CSV", "CSV 저장 완료: " + csvFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e("BLE_CSV", "CSV 저장 실패", e);
        }
    }
    public void saveSignalQualityHistoryToCsv() {
        List<BleDeviceData> unsavedSignalList = new ArrayList<>();

        for (BleDeviceData data : scanHistoryList) {
            if (!data.savedToSignalCsv) {
                unsavedSignalList.add(data);
            }
        }

        if (unsavedSignalList.isEmpty()) {
            Log.d("BLE_SIGNAL_CSV", "새로 저장할 신호 품질 데이터가 없음");
            return;
        }

        File dir = mainActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null) {
            Log.e("BLE_SIGNAL_CSV", "저장 폴더를 가져올 수 없음");
            return;
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File csvFile = new File(dir, "signal_quality.csv");
        boolean fileExists = csvFile.exists();

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            if (!fileExists) {
                writer.append("received_at,tx_power,rssi,path_loss\n");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            for (BleDeviceData data : unsavedSignalList) {
                String receivedAt = sdf.format(new Date(data.receivedAt));

                String txPowerStr = data.txPower != null ? String.valueOf(data.txPower) : "";
                String pathLossStr = data.pathLoss != null ? String.valueOf(data.pathLoss) : "";

                String row = String.format(Locale.getDefault(),
                        "%s,%s,%d,%s\n",
                        escapeCsv(receivedAt),
                        txPowerStr,
                        data.rssi,
                        pathLossStr
                );

                writer.append(row);
                data.savedToSignalCsv = true;
            }

            writer.flush();
            Log.d("BLE_SIGNAL_CSV", "신호 품질 CSV 저장 완료: " + csvFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e("BLE_SIGNAL_CSV", "신호 품질 CSV 저장 실패", e);
        }
    }
}

