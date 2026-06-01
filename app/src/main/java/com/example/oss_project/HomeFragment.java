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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class HomeFragment extends Fragment implements ble.BleCallback {

    private static final String TAG = "Submission";

    private MapView mapView;
    private KakaoMap kakaoMap;
    private MyLocationManager myLocationManager;
    private SensorMarkerManager sensorMarkerManager;
    private ble bleManager;

    private double currentLat = 36.6287;
    private double currentLon = 127.4606;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bleManager = new ble();
        bleManager.bleInitialize(this);

        mapView = view.findViewById(R.id.map_view);
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
            }

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
                        if (bleManager != null) {
                            bleManager.startScan();
                        }
                    }

                    @Override
                    public void onStopScanAndUpload(int sensorIndex, Long sensorId) {
                        if (bleManager != null) {
                            bleManager.stopScan();
                        }

                        uploadDataToSubmissions(sensorIndex, sensorId);
                    }
                });

                kakaoMap.moveCamera(
                        CameraUpdateFactory.newCenterPosition(
                                LatLng.from(currentLat, currentLon),
                                16
                        )
                );

                sensorMarkerManager.addSensorMarkers();

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).checkPermissionsAndStart();
                }
            }
        });

        return view;
    }

    private void uploadDataToSubmissions(int sensorIndex, Long sensorId) {
        if (sensorMarkerManager == null || getContext() == null) {
            Log.e(TAG, "sensorMarkerManager 또는 context가 null입니다.");
            return;
        }

        if (sensorId == null) {
            Log.e(TAG, "sensorId가 null입니다.");
            Toast.makeText(getContext(), "센서 ID가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sensorIndex < 0 || sensorIndex >= SensorMarkerManager.SENSOR_MAC_ADDRESSES.length) {
            Log.e(TAG, "잘못된 sensorIndex: " + sensorIndex);
            Toast.makeText(getContext(), "잘못된 센서 인덱스입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String macAddress = SensorMarkerManager.SENSOR_MAC_ADDRESSES[sensorIndex];
        BleDeviceData data = sensorMarkerManager.getSensorData(macAddress);

        float temperature;
        float humidity;
        int eco2;
        int airQuality;
        int rssi;

        if (data == null) {
            Log.w(TAG, "BLE 데이터 없음. 더미 데이터로 submissions 전송합니다. macAddress = " + macAddress);

            temperature = 25.3f;
            humidity = 45.0f;
            eco2 = 600;
            airQuality = 80;
            rssi = -65;

            Toast.makeText(getContext(), "BLE 데이터 없음: 더미 데이터 전송", Toast.LENGTH_SHORT).show();
        } else {
            temperature = data.temp;
            humidity = data.humidity;
            eco2 = data.eco2;
            airQuality = data.aqi;
            rssi = data.rssi;
        }

        android.content.SharedPreferences prefs =
                getContext().getSharedPreferences("auth_pref", android.content.Context.MODE_PRIVATE);

        String token = prefs.getString("access_token", null);

        if (token == null || token.trim().isEmpty()) {
            Log.e(TAG, "access_token이 없습니다. 로그인 상태를 확인하세요.");
            Toast.makeText(getContext(), "로그인 토큰이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String measuredAt = createMeasuredAtNow();

        SubmissionRequest request = new SubmissionRequest(
                sensorId,
                temperature,
                humidity,
                eco2,
                airQuality,
                rssi,
                currentLat,
                currentLon,
                measuredAt
        );

        Log.d(TAG, "==== submissions 요청 준비 ====");
        Log.d(TAG, "sensorId = " + sensorId);
        Log.d(TAG, "temperature = " + temperature);
        Log.d(TAG, "humidity = " + humidity);
        Log.d(TAG, "eco2 = " + eco2);
        Log.d(TAG, "airQuality = " + airQuality);
        Log.d(TAG, "rssi = " + rssi);
        Log.d(TAG, "latitude = " + currentLat);
        Log.d(TAG, "longitude = " + currentLon);
        Log.d(TAG, "measuredAt = " + measuredAt);
        Log.d(TAG, "Authorization = Bearer " + token);

        ApiService service = RetrofitClient.getClient().create(ApiService.class);

        service.submitCollection("Bearer " + token, request)
                .enqueue(new Callback<ApiResult<Response>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResult<Response>> call,
                            retrofit2.Response<ApiResult<Response>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "수집 성공 responseCode = " + response.code());

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "수집 성공!", Toast.LENGTH_SHORT).show()
                                );
                            }
                        } else {
                            Log.e(TAG, "수집 실패 responseCode = " + response.code());
                            Log.e(TAG, "requestUrl = " + call.request().url());

                            try {
                                if (response.errorBody() != null) {
                                    Log.e(TAG, "errorBody = " + response.errorBody().string());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "errorBody 읽기 실패", e);
                            }

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "수집 실패: " + response.code(), Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResult<Response>> call, Throwable t) {
                        Log.e(TAG, "수집 요청 자체 실패");
                        Log.e(TAG, "requestUrl = " + call.request().url());
                        Log.e(TAG, "errorMessage = " + t.getMessage(), t);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
    }

    private String createMeasuredAtNow() {
        return new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.KOREA
        ).format(new Date());
    }

    public void startGpsAndScan() {
        if (myLocationManager != null) {
            myLocationManager.addMyLocationMarker(currentLat, currentLon);

            myLocationManager.startGps((lat, lon) -> {
                currentLat = lat;
                currentLon = lon;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            myLocationManager.updateLocation(lat, lon)
                    );
                }
            });
        }

        if (bleManager != null) {
            bleManager.startScan();
        }
    }

    @Override
    public void onFilteredBleDataUpdated(List<BleDeviceData> dataList, List<String> discoveredUuids) {
        if (sensorMarkerManager != null) {
            sensorMarkerManager.updateSensorData(dataList);
        }
    }

    @Override
    public void runOnUiThread(Runnable action) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(action);
        }
    }

    @Override
    public android.app.Activity getBleActivity() {
        return getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mapView != null) {
            mapView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mapView != null) {
            mapView.pause();
        }
    }
}