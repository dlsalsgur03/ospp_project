package com.example.oss_project;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.oss_project.api.RetrofitClient;
import com.example.oss_project.api.comm_data;
import com.example.oss_project.api.postdata;
import com.example.oss_project.api.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_REQUEST_CODE_S = 101;

    private Button btnScan, btnStop, btnSave;
    private TextView tvLog;
    private android.widget.LinearLayout layoutScanRecords;
    private ble bleManager;

    //안준석
    private double currentLat = 0.0;
    private double currentLon = 0.0;
    private boolean isSending = false;
    private android.widget.LinearLayout layoutApiSendRecords;
    private java.util.Map<String, BleDeviceData> lastSentData = new java.util.HashMap<>();
    // 마지막으로 보낸 데이터 매핑함 혹시 나중에 기기 여러개 사용할 수도 있으니까 mac 별로 postdata 매핑했음.

    private android.widget.Spinner spinnerUuidFilter;
    private android.widget.EditText etNameFilter;
    private android.widget.EditText etRssiFilter;
    private Button btnApplyFilter, btnClearFilter;

    private android.widget.ArrayAdapter<String> uuidAdapter;
    private final java.util.List<String> uuidDisplayList = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLog = findViewById(R.id.tv_log);
        spinnerUuidFilter = findViewById(R.id.spinner_uuid_filter);
        etNameFilter = findViewById(R.id.et_name_filter);
        etRssiFilter = findViewById(R.id.et_rssi_filter);
        btnApplyFilter = findViewById(R.id.btn_apply_filter);
        btnClearFilter = findViewById(R.id.btn_clear_filter);
        layoutScanRecords = findViewById(R.id.layout_scan_records);

        tvLog.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        uuidDisplayList.add("전체");
        uuidAdapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                uuidDisplayList
        );
        uuidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUuidFilter.setAdapter(uuidAdapter);

        btnScan = findViewById(R.id.btn_scan);
        btnStop = findViewById(R.id.btn_stop);
        btnSave = findViewById(R.id.btn_save);

        if (findViewById(R.id.btn_view) != null) {
            findViewById(R.id.btn_view).setOnClickListener(v -> showCsvData());
        }

        bleManager = new ble();
        bleManager.bleInitialize(this);

        checkBlePermissions(this);

        btnScan.setOnClickListener(v -> {
            bleManager.startScan();
            tvLog.setText("스캔 시작됨...");
        });

        btnStop.setOnClickListener(v -> {
            bleManager.stopScan();
            tvLog.setText("스캔 중지됨.");
        });

        btnSave.setOnClickListener(v -> {
            bleManager.saveFilteredHistoryToCsv();
            bleManager.saveSignalQualityHistoryToCsv();

            File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir != null) {
                tvLog.append("\n[저장 요청] 경로: " + dir.getAbsolutePath());
            } else {
                tvLog.append("\n[저장 실패] 저장 폴더를 찾을 수 없음");
            }
        });

        Button btnRefresh = findViewById(R.id.btn_refresh);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                layoutScanRecords.removeAllViews();
                bleManager.clearScannedData();
                tvLog.setText("스캔 기록이 초기화되었습니다.");
            });
        }

        btnApplyFilter.setOnClickListener(v -> {
            String selectedUuid = null;

            Object selectedItem = spinnerUuidFilter.getSelectedItem();
            if (selectedItem != null) {
                String selectedText = selectedItem.toString();
                if (!selectedText.equals("전체")) {
                    selectedUuid = selectedText;
                }
            }

            String keyword = etNameFilter.getText() != null
                    ? etNameFilter.getText().toString().trim()
                    : "";

            Integer minRssi = null;
            String rssiText = etRssiFilter.getText() != null
                    ? etRssiFilter.getText().toString().trim()
                    : "";

            if (!rssiText.isEmpty()) {
                try {
                    minRssi = Integer.parseInt(rssiText);
                } catch (NumberFormatException e) {
                    tvLog.setText("RSSI 값이 올바르지 않음");
                    return;
                }
            }

            bleManager.setUuidFilter(selectedUuid);
            bleManager.setNameKeywordFilter(keyword);
            bleManager.setMinRssiFilter(minRssi);

            tvLog.setText("필터 적용됨");
        });

        // 여기로 이동: 필터 해제 버튼은 onCreate에서 한 번만 설정
        btnClearFilter.setOnClickListener(v -> {
            spinnerUuidFilter.setSelection(0);
            etNameFilter.setText("");
            etRssiFilter.setText("");

            bleManager.setUuidFilter(null);
            bleManager.setNameKeywordFilter("");
            bleManager.setMinRssiFilter(null);

            tvLog.setText("필터 해제됨");
        });


        // 안준석
        Button btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            isSending = !isSending;
            btnSend.setText(isSending ? "전송 중지" : "전송 시작");
            tvLog.append(isSending ? "\n전송 시작됨" : "\n전송 중지됨");
        });

        layoutApiSendRecords = findViewById(R.id.api_send_records);

        startGps();
    }

    // 안준석 GPS
    private void startGps(){
        android.location.LocationManager locationManager =
                (android.location.LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(
                    android.location.LocationManager.GPS_PROVIDER,
                    1000,   // 1초마다 갱신
                    1,      // 1미터 이동시 갱신
                    location -> {
                        currentLat = location.getLatitude();
                        currentLon = location.getLongitude();
                    }
            );
        }
    }



    // 안준석 로그 추가
    private void appendSendLog(boolean success, String mac, Response response, int code) {
        String time = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());

        String line;
        if (success && response != null) {
            line = String.format(
                    "[%s]\n" +
                            "{\n" +
                            "  \"result\": \"%s\",\n" +
                            "  \"message\": \"%s\",\n" +
                            "  \"received_data\": {\n" +
                            "    \"team\": \"%s\",\n" +
                            "    \"sensor\": \"%s\"\n" +
                            "  },\n" +
                            "  \"mac\": \"%s\"\n" +
                            "}",
                    time,
                    response.result != null ? response.result : "-",
                    response.message != null ? response.message : "-",
                    response.receivedData != null ? response.receivedData.team : "-",
                    response.receivedData != null ? response.receivedData.sensor : "-",
                    mac
            );
        } else {
            line = String.format(
                    "[%s]\n" +
                            "{\n" +
                            "  \"result\": \"Failed\",\n" +
                            "  \"http_code\": %d,\n" +
                            "  \"mac\": \"%s\"\n" +
                            "}",
                    time, code, mac
            );
        }

        TextView logView = new TextView(this);
        logView.setText(line);
        logView.setTextSize(12);
        logView.setPadding(16, 12, 16, 12);
        logView.setTextColor(success ? android.graphics.Color.parseColor("#006600") : android.graphics.Color.RED);
        logView.setBackgroundColor(success
                ? android.graphics.Color.parseColor("#E8F5E9")
                : android.graphics.Color.parseColor("#FFEBEE"));

        android.view.View divider = new android.view.View(this);
        divider.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(android.graphics.Color.LTGRAY);

        layoutApiSendRecords.addView(divider, 0);
        layoutApiSendRecords.addView(logView, 0);

        while (layoutApiSendRecords.getChildCount() > 100) {
            layoutApiSendRecords.removeViewAt(layoutApiSendRecords.getChildCount() - 1);
        }
    }

    private boolean isDataChanged(BleDeviceData prev, BleDeviceData curr) {
        return prev.timestampValue != curr.timestampValue ||
                prev.temp           != curr.temp           ||
                prev.humidity       != curr.humidity       ||
                prev.aqi            != curr.aqi            ||
                prev.tvoc           != curr.tvoc           ||
                prev.eco2           != curr.eco2;
    }
    public void onFilteredBleDataUpdated(List<BleDeviceData> dataList, List<String> discoveredUuids) {
        updateUuidSpinner(discoveredUuids);
        layoutScanRecords.removeAllViews();

        // 반복문 밖에서 한 번만 생성 안준석
        comm_data service = RetrofitClient.getClient().create(comm_data.class);

        for (BleDeviceData data : dataList) {
            TextView newView = new TextView(this);

            double A = -50.0;
            double n = 2.8;
            double distance = Math.pow(10, (A - data.rssi) / (10 * n));

            String finalInfo =
                    "이름: " + data.deviceName +
                            "\nUUID: " + data.serviceUuid +
                            "\nRSSI: " + data.rssi + " dBm" +
                            "\nRaw Data (Hex): " + data.rawHex +
                            "\n추정 거리: " + String.format(Locale.US, "%.2f", distance) + "m" +
                            "\n온도: " + String.format(Locale.US, "%.2f", data.temp) + "°C" +
                            "\n습도: " + String.format(Locale.US, "%.2f", data.humidity) + "%" +
                            "\nAQI: " + data.aqi +
                            "\nTVOC: " + data.tvoc +
                            "\neCO2: " + data.eco2 + " ppm" +
                            "\nPayload Timestamp: " + data.timestampValue +
                            "\nMAC: " + data.deviceAddress;

            newView.setText(finalInfo);
            newView.setTextSize(16);
            newView.setPadding(20, 20, 20, 40);
            newView.setTextColor(android.graphics.Color.BLACK);

            layoutScanRecords.addView(newView, 0);

            // 서버 전송 안준석
            if (isSending) {
                BleDeviceData prev = lastSentData.get(data.deviceAddress);
                if (prev == null || isDataChanged(prev, data)){
                    lastSentData.put(data.deviceAddress, data);
                    postdata pd = RetrofitClient.makePostData(data, currentLat, currentLon,
                            Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));


                service.post_json(pd).enqueue(new retrofit2.Callback<Response>() {
                    @Override
                    public void onResponse(retrofit2.Call<Response> call,
                                           retrofit2.Response<Response> response) {
                        int code = response.code(); // 200, 404, 500 등

                        if (response.isSuccessful()) {
                            Response body = response.body();
                            boolean success = body != null && body.isSuccess();
                            Log.d("서버전송", "성공 " + code + ": " + data.deviceAddress);
                            runOnUiThread(() -> appendSendLog(success, data.deviceAddress, body, code));
                        } else {
                            // 에러일 때 바디 읽기
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                errorBody = "바디 읽기 실패";
                            }

                            String finalErrorBody = errorBody;
                            Log.e("서버전송", "HTTP " + code + ": " + finalErrorBody);
                            runOnUiThread(() -> appendSendLog(false, data.deviceAddress, null, code));
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Response> call, Throwable t) {
                        Log.e("서버전송", "네트워크 실패: " + t.getMessage());
                        runOnUiThread(() -> appendSendLog(false, data.deviceAddress, null, 0));
                    }
                });
            }
            }
        }
        tvLog.setText(
                "수신 장치 수: " + dataList.size() +
                        "\n발견 UUID 수: " + discoveredUuids.size()
        );
    }


    public void showCsvData() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        StringBuilder sb = new StringBuilder();

        if (dir == null || !dir.exists()) {
            sb.append("저장 폴더가 없습니다.");
        } else {
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));

            if (files == null || files.length == 0) {
                sb.append("저장된 CSV 파일이 없습니다.");
            } else {
                for (File file : files) {
                    sb.append("=== ").append(file.getName()).append(" ===\n");

                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        int count = 0;
                        while ((line = br.readLine()) != null && count < 20) {
                            sb.append(line).append("\n");
                            count++;
                        }
                    } catch (IOException e) {
                        sb.append("파일 읽기 오류: ").append(file.getName()).append("\n");
                    }

                    sb.append("\n");
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("CSV 데이터 미리보기")
                .setMessage(sb.toString())
                .setPositiveButton("닫기", null)
                .show();
    }

    public void checkBlePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestBlePermissions(activity);
            }
        }
    }

    private void requestBlePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_REQUEST_CODE_S);
        }
    }

    private void updateUuidSpinner(List<String> discoveredUuids) {
        if (spinnerUuidFilter == null || uuidAdapter == null) return;

        String currentSelection = null;
        Object selectedItem = spinnerUuidFilter.getSelectedItem();
        if (selectedItem != null) {
            currentSelection = selectedItem.toString();
        }

        java.util.List<String> newItems = new java.util.ArrayList<>();
        newItems.add("전체");

        if (discoveredUuids != null) {
            for (String uuid : discoveredUuids) {
                if (uuid != null && !uuid.trim().isEmpty() && !newItems.contains(uuid)) {
                    newItems.add(uuid);
                }
            }
        }

        boolean changed = uuidDisplayList.size() != newItems.size() || !uuidDisplayList.equals(newItems);
        if (!changed) return;

        uuidDisplayList.clear();
        uuidDisplayList.addAll(newItems);
        uuidAdapter.notifyDataSetChanged();

        if (currentSelection != null && uuidDisplayList.contains(currentSelection)) {
            spinnerUuidFilter.setSelection(uuidDisplayList.indexOf(currentSelection));
        } else {
            spinnerUuidFilter.setSelection(0);
        }
    }
}