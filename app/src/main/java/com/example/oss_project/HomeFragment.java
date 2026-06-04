package com.example.oss_project;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.RetrofitClient;
import com.example.oss_project.api.SubmissionRequest;
import com.example.oss_project.api.SubmissionResponse;
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

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).checkPermissionsAndStart();
        }

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
                    public void onSubmitSensor(int sensorIndex, SensorMarkerManager.SubmissionCallback callback) {
                        submitSensorData(sensorIndex, callback);
                    }
                });

                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(currentLat, currentLon), 16));
                sensorMarkerManager.addSensorMarkers();
                startGps();

            }
        });

        return view;
    }

    private void submitSensorData(int sensorIndex, SensorMarkerManager.SubmissionCallback callback) {
        if (getContext() == null) {
            callback.onError("화면 상태가 올바르지 않습니다.");
            return;
        }

        SharedPreferences prefs = getContext().getSharedPreferences("auth_pref", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null || token.isEmpty()) {
            callback.onError("로그인 정보가 없습니다. 다시 로그인해 주세요.");
            return;
        }

        // 실제 스캔된 데이터 가져오기
        String macAddress = SensorMarkerManager.SENSOR_MAC_ADDRESSES[sensorIndex];
        BleDeviceData realData = sensorMarkerManager.getSensorData(macAddress);

        if (realData == null) {
            callback.onError("센서 신호가 감지되지 않았습니다. 센서 근처에서 다시 시도해 주세요.");
            Log.e("ServerUpload", "데이터 없음: MAC=" + macAddress);
            return;
        }

        String measuredAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date());
        SubmissionRequest request = new SubmissionRequest(
                sensorIndex + 1L,
                realData.temp,
                realData.humidity,
                realData.eco2,
                realData.aqi,
                realData.rssi,
                currentLat,
                currentLon,
                measuredAt
        );

        Log.d("ServerUpload", "실제 BLE 데이터 submission 전송"
                + ": sensorIndex=" + sensorIndex
                + ", mac=" + macAddress
                + ", temp=" + realData.temp);

        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.submitSensorData("Bearer " + token, request).enqueue(new Callback<ApiResult<SubmissionResponse>>() {
            @Override
            public void onResponse(Call<ApiResult<SubmissionResponse>> call, retrofit2.Response<ApiResult<SubmissionResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    SubmissionResponse data = response.body().data;
                    Log.d("ServerUpload", "실제 데이터 submission 전송 성공"
                            + ": submissionId=" + data.submissionId);
                    runOnUiThread(() -> callback.onSuccess(data));
                } else {
                    Log.e("ServerUpload", "서버 응답 오류 코드: " + response.code());
                    runOnUiThread(() -> callback.onError("전송 실패: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<ApiResult<SubmissionResponse>> call, Throwable t) {
                Log.e("ServerUpload", "통신 에러: " + t.getMessage());
                runOnUiThread(() -> callback.onError("통신 에러: " + t.getMessage()));
            }
        });
    }


    public void startGps() {
        if (myLocationManager != null) {
            myLocationManager.addMyLocationMarker(currentLat, currentLon);
            myLocationManager.startGps((lat, lon) -> {
                currentLat = lat;
                currentLon = lon;

                if (sensorMarkerManager != null) {
                    sensorMarkerManager.updateCurrentLocation(lat, lon);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            myLocationManager.updateLocation(lat, lon));
                }
            });
        }
    }
    public void startBleScan() {
        if (bleManager != null) {
            bleManager.startScan();
        }
    }

    public void stopBleScan() {
        if (bleManager != null) bleManager.stopScan();
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
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBleScan();
        if (mapView != null) mapView.pause();
    }

    @Override
    public void onDestroyView() {
        stopBleScan();
        super.onDestroyView();
    }

}
