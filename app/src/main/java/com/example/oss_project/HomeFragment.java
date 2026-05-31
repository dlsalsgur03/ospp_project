package com.example.oss_project;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.oss_project.api.RetrofitClient;
import com.example.oss_project.api.comm_data;
import com.example.oss_project.api.postdata;
import com.example.oss_project.api.Response;
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

                // 마커 수집 이벤트 리스너 연결
                sensorMarkerManager.setSensorCollectListener(new SensorMarkerManager.SensorCollectListener() {
                    @Override
                    public void onStartScan() {
                        if (bleManager != null) {
                            bleManager.startScan();
                            Log.d("HomeFragment", "BLE 스캔 시작 (수집)");
                        }
                    }

                    @Override
                    public void onStopScanAndUpload(int sensorIndex) {
                        if (bleManager != null) {
                            bleManager.stopScan();
                            Log.d("HomeFragment", "BLE 스캔 중지 및 서버 전송 요청");
                            
                            // 서버 전송 로직 수행
                            uploadDataToServer(sensorIndex);
                        }
                    }
                });

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

    private void uploadDataToServer(int sensorIndex) {
        if (sensorMarkerManager == null || getContext() == null) {
            Log.e("ServerUpload", "매니저 또는 컨텍스트가 null입니다.");
            return;
        }

        // 1. 해당 센서의 MAC 주소 가져오기
        String macAddress = SensorMarkerManager.SENSOR_MAC_ADDRESSES[sensorIndex];
        Log.d("ServerUpload", "전송 시도 - 센서 인덱스: " + sensorIndex + ", 목표 MAC: " + macAddress);

        // 2. 수집된 데이터 맵에서 최신 데이터 추출
        BleDeviceData latestData = sensorMarkerManager.getSensorData(macAddress);

        if (latestData != null) {
            Log.d("ServerUpload", "데이터 발견! 서버 전송을 시작합니다. (온도: " + latestData.temp + ")");
            // 3. 서버 전송용 데이터 객체 생성
            String androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            postdata pd = RetrofitClient.makePostData(latestData, currentLat, currentLon, androidId);

            // 4. Retrofit을 이용한 서버 전송
            comm_data service = RetrofitClient.getClient().create(comm_data.class);
            service.post_json(pd).enqueue(new Callback<Response>() {
                @Override
                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                    Log.d("ServerUpload", "서버 응답 수신 - 코드: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("ServerUpload", "성공 메시지: " + response.body().message);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> 
                                android.widget.Toast.makeText(getContext(), "데이터 전송 성공!", android.widget.Toast.LENGTH_SHORT).show()
                            );
                        }
                    } else {
                        Log.e("ServerUpload", "전송 실패 (서버 에러)");
                    }
                }

                @Override
                public void onFailure(Call<Response> call, Throwable t) {
                    Log.e("ServerUpload", "네트워크 실패: " + t.getMessage());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                            android.widget.Toast.makeText(getContext(), "네트워크 에러 발생", android.widget.Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        } else {
            Log.w("ServerUpload", "전송 실패: 수집된 데이터가 없습니다. (MAC: " + macAddress + ")");
            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                    android.widget.Toast.makeText(getContext(), "수집된 데이터가 없습니다. 센서 근처에서 다시 시도해주세요.", android.widget.Toast.LENGTH_SHORT).show()
                );
            }
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
