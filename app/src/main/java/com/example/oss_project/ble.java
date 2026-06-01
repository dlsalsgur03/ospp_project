package com.example.oss_project;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ble {
    private static final String TAG = "BLE_SCAN";
    private static final ParcelUuid ENVIRONMENTAL_SENSOR_UUID =
            ParcelUuid.fromString("0000181a-0000-1000-8000-00805f9b34fb");

    public interface BleCallback {
        void onFilteredBleDataUpdated(List<BleDeviceData> dataList, List<String> discoveredUuids);
        void runOnUiThread(Runnable action);
        Activity getBleActivity();
    }

    private BleCallback callback;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning = false;
    private boolean legacyScanning = false;

    private final Map<String, BleDeviceData> latestEnvironmentalData = new HashMap<>();
    private int environmentalScanCount = 0;

    public void bleInitialize(BleCallback callback) {
        this.callback = callback;
        refreshBluetoothObjects();
        Log.d(TAG, "BLE manager initialized");
    }

    public void startScan() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.d(TAG, "startScan skipped: Activity is null");
            return;
        }

        if (scanning || legacyScanning) {
            Log.d(TAG, "startScan skipped: already scanning");
            return;
        }

        if (!hasPermission(activity, Manifest.permission.BLUETOOTH_SCAN)) {
            Log.d(TAG, "startScan failed: BLUETOOTH_SCAN permission missing");
            return;
        }

        if (!refreshBluetoothObjects()) {
            return;
        }

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        try {
            bluetoothLeScanner.startScan(null, settings, scanCallback);
            scanning = true;
            environmentalScanCount = 0;
            Log.d(TAG, "BLE scan started. environmentalUuid=" + ENVIRONMENTAL_SENSOR_UUID);
        } catch (Exception e) {
            Log.e(TAG, "BLE scanner start failed. Trying legacy scan.", e);
            startLegacyScan();
        }
    }

    public void stopScan() {
        Activity activity = getActivity();

        if (activity != null && hasPermission(activity, Manifest.permission.BLUETOOTH_SCAN)) {
            if (bluetoothLeScanner != null && scanning) {
                try {
                    bluetoothLeScanner.stopScan(scanCallback);
                } catch (Exception e) {
                    Log.e(TAG, "BLE scanner stop failed", e);
                }
            }

            if (bluetoothAdapter != null && legacyScanning) {
                try {
                    bluetoothAdapter.stopLeScan(legacyScanCallback);
                } catch (Exception e) {
                    Log.e(TAG, "Legacy BLE scan stop failed", e);
                }
            }
        }

        scanning = false;
        legacyScanning = false;
        Log.d(TAG, "BLE scan stopped. environmentalCount=" + environmentalScanCount);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Activity activity = getActivity();
            if (activity == null) {
                Log.d(TAG, "scan result ignored: Activity is null");
                return;
            }

            if (!hasPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)) {
                Log.d(TAG, "scan result ignored: BLUETOOTH_CONNECT permission missing");
                return;
            }

            BluetoothDevice device = result.getDevice();
            ScanRecord record = result.getScanRecord();
            int rssi = result.getRssi();

            if (record == null) {
                Log.d(TAG, "scan result ignored: ScanRecord is null, rssi=" + rssi);
                return;
            }

            String deviceAddress = getDeviceAddress(device);
            String deviceName = getDeviceName(device, record.getDeviceName());
            Integer txPower = record.getTxPowerLevel() != Integer.MIN_VALUE
                    ? record.getTxPowerLevel()
                    : null;
            Map<ParcelUuid, byte[]> serviceDataMap = record.getServiceData();

            Log.d(TAG, "scan result"
                    + ": source=scanner"
                    + ", mac=" + deviceAddress
                    + ", name=" + deviceName
                    + ", rssi=" + rssi
                    + ", txPower=" + txPower
                    + ", serviceUuids=" + record.getServiceUuids()
                    + ", serviceData=" + serviceDataToString(serviceDataMap)
                    + ", raw=" + bytesToHex(record.getBytes()));

            handleServiceData(deviceAddress, deviceName, rssi, txPower, serviceDataMap, "scanner");
        }

        @Override
        public void onScanFailed(int errorCode) {
            scanning = false;
            Log.e(TAG, "BLE scan failed: errorCode=" + errorCode + ", reason=" + scanFailureReason(errorCode));

            if (errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                startLegacyScan();
            }
        }
    };

    private final BluetoothAdapter.LeScanCallback legacyScanCallback = (device, rssi, scanRecordBytes) -> {
        Activity activity = getActivity();
        if (activity == null) {
            Log.d(TAG, "legacy scan result ignored: Activity is null");
            return;
        }

        if (!hasPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.d(TAG, "legacy scan result ignored: BLUETOOTH_CONNECT permission missing");
            return;
        }

        String deviceAddress = getDeviceAddress(device);
        LegacyAdvertisement advertisement = parseLegacyAdvertisement(scanRecordBytes);
        String deviceName = getDeviceName(device, advertisement.name);

        Log.d(TAG, "scan result"
                + ": source=legacy"
                + ", mac=" + deviceAddress
                + ", name=" + deviceName
                + ", rssi=" + rssi
                + ", txPower=" + advertisement.txPower
                + ", serviceUuids=" + advertisement.serviceUuids
                + ", serviceData=" + serviceDataToString(advertisement.serviceDataMap)
                + ", raw=" + bytesToHex(scanRecordBytes));

        handleServiceData(
                deviceAddress,
                deviceName,
                rssi,
                advertisement.txPower,
                advertisement.serviceDataMap,
                "legacy"
        );
    };

    private void startLegacyScan() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.d(TAG, "legacy scan skipped: Activity is null");
            return;
        }

        if (!hasPermission(activity, Manifest.permission.BLUETOOTH_SCAN)) {
            Log.d(TAG, "legacy scan failed: BLUETOOTH_SCAN permission missing");
            return;
        }

        if (bluetoothAdapter == null && !refreshBluetoothObjects()) {
            return;
        }

        try {
            boolean started = bluetoothAdapter.startLeScan(legacyScanCallback);
            legacyScanning = started;
            environmentalScanCount = 0;
            Log.d(TAG, started
                    ? "Legacy BLE scan started"
                    : "Legacy BLE scan failed: startLeScan returned false");
        } catch (Exception e) {
            legacyScanning = false;
            Log.e(TAG, "Legacy BLE scan start failed", e);
        }
    }

    private void handleServiceData(String deviceAddress, String deviceName, int rssi, Integer txPower,
                                   Map<ParcelUuid, byte[]> serviceDataMap, String source) {
        if (serviceDataMap == null || serviceDataMap.isEmpty()) {
            return;
        }

        boolean updated = false;
        for (Map.Entry<ParcelUuid, byte[]> entry : serviceDataMap.entrySet()) {
            ParcelUuid uuid = entry.getKey();
            byte[] serviceData = entry.getValue();

            if (!ENVIRONMENTAL_SENSOR_UUID.equals(uuid)) {
                continue;
            }

            if (serviceData == null) {
                continue;
            }

            BleDeviceData data = parseEnvironmentalSensorData(
                    deviceAddress,
                    deviceName,
                    uuid.toString(),
                    serviceData,
                    rssi,
                    txPower
            );

            latestEnvironmentalData.put(deviceAddress + "_" + uuid, data);
            environmentalScanCount++;
            updated = true;

            Log.d(TAG, "environmental sensor found"
                    + ": source=" + source
                    + ", count=" + environmentalScanCount
                    + ", mac=" + data.deviceAddress
                    + ", name=" + data.deviceName
                    + ", rssi=" + data.rssi
                    + ", uuid=" + data.serviceUuid
                    + ", rawHex=" + data.rawHex
                    + ", temp=" + data.temp
                    + ", humidity=" + data.humidity
                    + ", aqi=" + data.aqi
                    + ", tvoc=" + data.tvoc
                    + ", eco2=" + data.eco2
                    + ", timestamp=" + data.timestampValue
                    + ", timeValid=" + data.isTimeValid);
        }

        if (updated) {
            notifyDataUpdated();
        }
    }

    private BleDeviceData parseEnvironmentalSensorData(String deviceAddress, String deviceName,
                                                       String serviceUuid, byte[] serviceData,
                                                       int rssi, Integer txPower) {
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
            timeDiffSec = (System.currentTimeMillis() / 1000L) - payloadTimestamp;
            isTimeValid = Math.abs(timeDiffSec) <= 60;
        } else {
            Log.w(TAG, "environmental serviceData too short"
                    + ": bytes=" + serviceData.length
                    + ", rawHex=" + bytesToHex(serviceData));
        }

        Integer pathLoss = txPower != null ? txPower - rssi : null;
        return new BleDeviceData(
                deviceAddress,
                deviceName,
                serviceUuid,
                bytesToHex(serviceData),
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
                pathLoss,
                timeDiffSec,
                isTimeValid
        );
    }

    private void notifyDataUpdated() {
        if (callback == null) return;

        List<BleDeviceData> dataList = new ArrayList<>(latestEnvironmentalData.values());
        List<String> discoveredUuids = new ArrayList<>();
        for (BleDeviceData data : dataList) {
            if (data.serviceUuid != null && !discoveredUuids.contains(data.serviceUuid)) {
                discoveredUuids.add(data.serviceUuid);
            }
        }

        callback.runOnUiThread(() -> callback.onFilteredBleDataUpdated(dataList, discoveredUuids));
    }

    private boolean refreshBluetoothObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d(TAG, "BluetoothAdapter is null");
            bluetoothLeScanner = null;
            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is disabled");
            bluetoothLeScanner = null;
            return false;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            Log.d(TAG, "BluetoothLeScanner is null");
            return false;
        }

        return true;
    }

    private LegacyAdvertisement parseLegacyAdvertisement(byte[] bytes) {
        LegacyAdvertisement advertisement = new LegacyAdvertisement();
        if (bytes == null) return advertisement;

        int index = 0;
        while (index < bytes.length) {
            int length = bytes[index++] & 0xFF;
            if (length == 0) break;
            if (index + length > bytes.length) break;

            int type = bytes[index++] & 0xFF;
            int dataStart = index;
            int dataLength = length - 1;

            if (type == 0x08 || type == 0x09) {
                advertisement.name = new String(bytes, dataStart, dataLength);
            } else if (type == 0x0A && dataLength >= 1) {
                advertisement.txPower = (int) bytes[dataStart];
            } else if (type == 0x02 || type == 0x03) {
                parse16BitServiceUuids(bytes, dataStart, dataLength, advertisement.serviceUuids);
            } else if (type == 0x16) {
                parse16BitServiceData(bytes, dataStart, dataLength, advertisement.serviceDataMap);
            }

            index = dataStart + dataLength;
        }

        return advertisement;
    }

    private void parse16BitServiceUuids(byte[] bytes, int start, int length, List<ParcelUuid> output) {
        for (int i = start; i + 1 < start + length; i += 2) {
            int uuid16 = (bytes[i] & 0xFF) | ((bytes[i + 1] & 0xFF) << 8);
            output.add(ParcelUuid.fromString(String.format(Locale.US,
                    "0000%04x-0000-1000-8000-00805f9b34fb", uuid16)));
        }
    }

    private void parse16BitServiceData(byte[] bytes, int start, int length,
                                       Map<ParcelUuid, byte[]> output) {
        if (length < 2) return;

        int uuid16 = (bytes[start] & 0xFF) | ((bytes[start + 1] & 0xFF) << 8);
        ParcelUuid uuid = ParcelUuid.fromString(String.format(Locale.US,
                "0000%04x-0000-1000-8000-00805f9b34fb", uuid16));

        byte[] serviceData = new byte[length - 2];
        System.arraycopy(bytes, start + 2, serviceData, 0, serviceData.length);
        output.put(uuid, serviceData);
    }

    private Activity getActivity() {
        return callback != null ? callback.getBleActivity() : null;
    }

    private boolean hasPermission(Activity activity, String permission) {
        return ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private String getDeviceAddress(BluetoothDevice device) {
        if (device == null || device.getAddress() == null) return "UNKNOWN";
        return device.getAddress().toUpperCase(Locale.US);
    }

    private String getDeviceName(BluetoothDevice device, String advertisedName) {
        if (advertisedName != null && !advertisedName.trim().isEmpty()) {
            return advertisedName;
        }

        if (device != null && device.getName() != null && !device.getName().trim().isEmpty()) {
            return device.getName();
        }

        return "Unknown";
    }

    private String scanFailureReason(int errorCode) {
        switch (errorCode) {
            case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                return "ALREADY_STARTED";
            case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                return "APPLICATION_REGISTRATION_FAILED";
            case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                return "INTERNAL_ERROR";
            case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                return "FEATURE_UNSUPPORTED";
            default:
                return "UNKNOWN";
        }
    }

    private String serviceDataToString(Map<ParcelUuid, byte[]> serviceDataMap) {
        if (serviceDataMap == null || serviceDataMap.isEmpty()) return "{}";

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<ParcelUuid, byte[]> entry : serviceDataMap.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(entry.getKey()).append("=").append(bytesToHex(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format(Locale.US, "%02X ", b));
        }
        return sb.toString().trim();
    }

    private static class LegacyAdvertisement {
        String name;
        Integer txPower;
        final List<ParcelUuid> serviceUuids = new ArrayList<>();
        final Map<ParcelUuid, byte[]> serviceDataMap = new HashMap<>();
    }
}
