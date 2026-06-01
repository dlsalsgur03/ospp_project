package com.example.oss_project;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.Response;
import com.example.oss_project.api.RetrofitClient;
import com.example.oss_project.api.SubmissionRequest;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraUpdateFactory;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class HomeFragment extends Fragment implements ble.BleCallback {

    private MapView mapView;
    private KakaoMap kakaoMap;
    private MyLocationManager myLocationManager;
    private SensorMarkerManager sensorMarkerManager;
    private ble bleManager;

    private double currentLat = 36.6287;
    private double currentLon = 127.4606;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bleManager = new ble();
        bleManager.bleInitialize(this);

        mapView = view.findViewById(R.id.map_view);
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {}
            @Override
            public void onMapError(Exception error) {
                Log.e("KakaoMap", "지도 에러: " + error.getMessage());
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                myLocationManager = new MyLocationManager(getContext(), kakaoMap);
                sensorMarkerManager = new SensorMarkerManager(getContext(), kakaoMap);

                sensorMarkerManager.setSensorCollectListener(new SensorMarkerManager.SensorCollectListener() {
                    @Override
                    public void onStartScan() {
                        if (bleManager != null) bleManager.startScan();
                    }

                    @Override
                    public void onStopScanAndUpload(int sensorIndex, Long sensorId) {
                        if (bleManager != null) bleManager.stopScan();
                        uploadDataToSubmissions(sensorIndex, sensorId);
                    }
                });

                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(currentLat, currentLon), 16));
                sensorMarkerManager.addSensorMarkers();

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).checkPermissionsAndStart();
                }
            }
        });

        return view;
    }

    private void uploadDataToSubmissions(int sensorIndex, Long sensorId) {
        if (sensorMarkerManager == null || getContext() == null) return;

        String macAddress = SensorMarkerManager.SENSOR_MAC_ADDRESSES[sensorIndex];
        BleDeviceData data = sensorMarkerManager.getSensorData(macAddress);

        if (data != null) {
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("auth_pref", android.content.Context.MODE_PRIVATE);
            String token = prefs.getString("access_token", null);

            // 서버 규격에 맞춘 9가지 필드 생성
            SubmissionRequest request = new SubmissionRequest(
                    sensorId,
                    data.temp,
                    data.humidity,
                    data.eco2,
                    data.aqi, // airQuality
                    data.rssi,
                    currentLat,
                    currentLon,
                    System.currentTimeMillis() // measuredAt
            );

            ApiService service = RetrofitClient.getClient().create(ApiService.class);
            service.submitCollection("Bearer " + token, request).enqueue(new Callback<ApiResult<Response>>() {
                @Override
                public void onResponse(Call<ApiResult<Response>> call, retrofit2.Response<ApiResult<Response>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "수집 성공!", Toast.LENGTH_SHORT).show());
                    } else {
                        Log.e("Submission", "실패 코드: " + response.code());
                    }
                }
                @Override
                public void onFailure(Call<ApiResult<Response>> call, Throwable t) {
                    Log.e("Submission", "에러: " + t.getMessage());
                }
            });
        } else {
            Toast.makeText(getContext(), "수집된 센서 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void startGpsAndScan() {
        if (myLocationManager != null) {
            myLocationManager.addMyLocationMarker(currentLat, currentLon);
            myLocationManager.startGps((lat, lon) -> {
                currentLat = lat; currentLon = lon;
                if (getActivity() != null) getActivity().runOnUiThread(() -> myLocationManager.updateLocation(lat, lon));
            });
        }
        if (bleManager != null) bleManager.startScan();
    }

    @Override
    public void onFilteredBleDataUpdated(List<BleDeviceData> dataList, List<String> discoveredUuids) {
        if (sensorMarkerManager != null) sensorMarkerManager.updateSensorData(dataList);
    }

    @Override
    public void runOnUiThread(Runnable action) {
        if (getActivity() != null) getActivity().runOnUiThread(action);
    }

    @Override
    public android.app.Activity getBleActivity() { return getActivity(); }

    @Override
    public void onResume() { super.onResume(); if (mapView != null) mapView.resume(); }

    @Override
    public void onPause() { super.onPause(); if (mapView != null) mapView.pause(); }
}
