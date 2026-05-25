package com.example.oss_project;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraUpdateFactory;

import java.util.List;

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

        // ble 초기화
        bleManager = new ble();
        bleManager.bleInitialize(this);

        // 지도 뷰 설정
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

                // 매니저 클래스 초기화
                myLocationManager = new MyLocationManager(getContext(), kakaoMap);
                sensorMarkerManager = new SensorMarkerManager(getContext(), kakaoMap);

                // 초기 카메라 위치 설정 및 센서 마커 추가
                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(currentLat, currentLon), 16));
                sensorMarkerManager.addSensorMarkers();

                // GPS 시작 (권한 체크는 Activity에서 수행 후 호출)
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).checkPermissionsAndStart();
                }
            }
        });

        return view;
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
        if (mapView != null) mapView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.pause();
    }
}
