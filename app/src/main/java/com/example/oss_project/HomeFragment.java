package com.example.oss_project;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.Response;
import com.example.oss_project.api.RetrofitClient;
import com.example.oss_project.api.comm_data;
import com.example.oss_project.api.postdata;
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
                        if (bleManager != null) {
                            bleManager.startScan();
                            Log.d("HomeFragment", "BLE 스캔 시작");
                        }
                    }

                    @Override
                    public void onStopScanAndUpload(int sensorIndex, Integer characterId) {
                        if (bleManager != null) {
                            bleManager.stopScan();
                            uploadDataToServer(sensorIndex, characterId);
                        }
                    }

                    @Override
                    public void onCancelScan() {
                        if (bleManager != null) {
                            bleManager.stopScan();
                            Log.d("HomeFragment", "수집 취소 - BLE 스캔 중지");
                        }
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

    private void uploadDataToServer(int sensorIndex, Integer characterId) {
        if (sensorMarkerManager == null || getContext() == null) return;

        // [변경] 특정 MAC 주소를 찾지 않고, 현재 잡힌 최신 데이터 아무거나 가져옵니다.
        String macAddress = SensorMarkerManager.SENSOR_MAC_ADDRESSES[sensorIndex];
        BleDeviceData latestData = null;
        
        // 센서 데이터 맵에서 가장 마지막에 들어온 데이터를 찾습니다.
        if (sensorMarkerManager.getSensorDataMap() != null && !sensorMarkerManager.getSensorDataMap().isEmpty()) {
            for (BleDeviceData data : sensorMarkerManager.getSensorDataMap().values()) {
                latestData = data; // 마지막 하나를 선택 (테스트용)
            }
        }

        if (latestData != null) {
            Log.d("HomeFragment", "테스트 모드: 잡힌 센서 데이터로 전송 시도 (MAC: " + latestData.deviceAddress + ")");
            String androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            postdata pd = RetrofitClient.makePostData(latestData, currentLat, currentLon, androidId, characterId);

            comm_data service = RetrofitClient.getClient().create(comm_data.class);
            service.post_json(pd).enqueue(new Callback<Response>() {
                @Override
                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("ServerUpload", "데이터 전송 성공: " + response.body().message);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "수집 및 전송 완료!", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Log.e("ServerUpload", "데이터 전송 실패 코드: " + response.code());
                    }
                }
                @Override
                public void onFailure(Call<Response> call, Throwable t) {
                    Log.e("ServerUpload", "통신 에러: " + t.getMessage());
                }
            });
        } else {
            Toast.makeText(getContext(), "수집된 센서 데이터가 없습니다. (MAC: " + macAddress + ")", Toast.LENGTH_SHORT).show();
        }
    }

    public void startGpsAndScan() {
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
        if (mapView != null) mapView.pause();
    }
}
