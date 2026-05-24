package com.example.oss_project;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private KakaoMap kakaoMap;
    private MyLocationManager myLocationManager;
    private SensorMarkerManager sensorMarkerManager;
    private ble bleManager;

    private double currentLat = 36.6287;
    private double currentLon = 127.4606;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Kakao SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_API_KEY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ble초기화
        bleManager = new ble();
        bleManager.bleInitialize(this);

        // 지도 뷰 설정
        mapView = findViewById(R.id.map_view);
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
                myLocationManager = new MyLocationManager(MainActivity.this, kakaoMap);
                sensorMarkerManager = new SensorMarkerManager(MainActivity.this, kakaoMap);

                // 초기 카메라 위치 설정 및 센서 마커 추가
                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(currentLat, currentLon), 16));
                sensorMarkerManager.addSensorMarkers();

                // GPS 시작 및 내 위치 마커 표시
                checkPermissions();
            }
        });

        // 하단 네비게이션 설정
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map) {
                // 지도 탭
            } else if (id == R.id.nav_ranking) {
                Intent intent = new Intent(MainActivity.this, RankingActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_book) {
                // 도감 탭
            } else if (id == R.id.nav_settings) {
                // 설정 탭
            }
            return true;
        });
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 100);
        } else {
            startGps();
            bleManager.startScan(); // BLE 스캔은 권한 확인 후 시작
        }
    }
    public void onFilteredBleDataUpdated(java.util.List<BleDeviceData> dataList,
                                         java.util.List<String> discoveredUuids) {
        if (sensorMarkerManager != null) {
            sensorMarkerManager.updateSensorData(dataList);
        }
    }

    private void startGps() {
        if (myLocationManager != null) {
            myLocationManager.addMyLocationMarker(currentLat, currentLon);
            myLocationManager.startGps((lat, lon) -> {
                currentLat = lat;
                currentLon = lon;
                runOnUiThread(() -> myLocationManager.updateLocation(lat, lon));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGps();
            bleManager.startScan();
        }
    }
}