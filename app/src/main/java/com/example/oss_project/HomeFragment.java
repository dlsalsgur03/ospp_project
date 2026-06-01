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

        SubmissionRequest request = createHardcodedSubmissionRequest(sensorIndex);
        Log.d("ServerUpload", "하드코딩 submission 전송"
                + ": sensorIndex=" + sensorIndex
                + ", sensorId=" + request.sensorId);

        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.submitSensorData("Bearer " + token, request).enqueue(new Callback<ApiResult<SubmissionResponse>>() {
            @Override
            public void onResponse(Call<ApiResult<SubmissionResponse>> call, retrofit2.Response<ApiResult<SubmissionResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    SubmissionResponse data = response.body().data;
                    Log.d("ServerUpload", "하드코딩 submission 전송 성공"
                            + ": submissionId=" + data.submissionId
                            + ", sensorId=" + data.sensorId
                            + ", rewardExp=" + data.rewardExp
                            + ", characterCollected=" + data.characterCollected);
                    runOnUiThread(() -> callback.onSuccess(data));
                } else {
                    Log.e("ServerUpload", "하드코딩 submission 전송 실패 코드: " + response.code());
                    runOnUiThread(() -> callback.onError("전송 실패: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<ApiResult<SubmissionResponse>> call, Throwable t) {
                Log.e("ServerUpload", "하드코딩 submission 통신 에러: " + t.getMessage());
                runOnUiThread(() -> callback.onError("통신 에러: " + t.getMessage()));
            }
        });
    }

    private SubmissionRequest createHardcodedSubmissionRequest(int sensorIndex) {
        float[] temps = {24.1f, 24.6f, 25.0f, 23.8f, 24.3f};
        float[] humidities = {42.0f, 44.5f, 41.2f, 45.0f, 43.3f};
        int[] aqis = {31, 37, 34, 40, 35};
        int[] eco2s = {520, 545, 538, 560, 532};
        int[] rssis = {-58, -62, -65, -60, -63};
        String measuredAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                .format(new Date());

        return new SubmissionRequest(
                sensorIndex + 1L,
                temps[sensorIndex],
                humidities[sensorIndex],
                eco2s[sensorIndex],
                aqis[sensorIndex],
                rssis[sensorIndex],
                currentLat,
                currentLon,
                measuredAt
        );
    }

    public void startGps() {
        if (myLocationManager != null) {
            myLocationManager.addMyLocationMarker(currentLat, currentLon);
            myLocationManager.startGps((lat, lon) -> {
                currentLat = lat;
                currentLon = lon;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> myLocationManager.updateLocation(lat, lon));
                }
            });
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
