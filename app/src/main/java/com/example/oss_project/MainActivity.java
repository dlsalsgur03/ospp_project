package com.example.oss_project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.kakao.vectormap.KakaoMapSdk;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private RankingFragment rankingFragment;
    private BookFragment bookFragment;
    private MyInfoFragment myInfoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Kakao SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_API_KEY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            rankingFragment = new RankingFragment();
            bookFragment = new BookFragment();
            myInfoFragment = new MyInfoFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, rankingFragment, "ranking").hide(rankingFragment)
                    .add(R.id.main_container, bookFragment, "book").hide(bookFragment)
                    .add(R.id.main_container, myInfoFragment, "my_info").hide(myInfoFragment)
                    .add(R.id.main_container, homeFragment, "home")
                    .commit();
        } else {
            homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
            rankingFragment = (RankingFragment) getSupportFragmentManager().findFragmentByTag("ranking");
            bookFragment = (BookFragment) getSupportFragmentManager().findFragmentByTag("book");
            myInfoFragment = (MyInfoFragment) getSupportFragmentManager().findFragmentByTag("my_info");

            // 복구 상황 대비: 변수가 null이면 다시 생성
            if (homeFragment == null) homeFragment = new HomeFragment();
            if (rankingFragment == null) rankingFragment = new RankingFragment();
            if (bookFragment == null) bookFragment = new BookFragment();
            if (myInfoFragment == null) myInfoFragment = new MyInfoFragment();
        }

        // 하단 네비게이션 설정
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // 복구 상황 대비: 변수가 null이면 다시 찾음
            if (homeFragment == null) homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
            if (rankingFragment == null) rankingFragment = (RankingFragment) getSupportFragmentManager().findFragmentByTag("ranking");
            if (bookFragment == null) bookFragment = (BookFragment) getSupportFragmentManager().findFragmentByTag("book");
            if (myInfoFragment == null) myInfoFragment = (MyInfoFragment) getSupportFragmentManager().findFragmentByTag("my_info");

            if (id == R.id.nav_map && homeFragment != null) {
                getSupportFragmentManager().beginTransaction().show(homeFragment).hide(rankingFragment).hide(bookFragment).hide(myInfoFragment).commit();
                return true;
            } else if (id == R.id.nav_ranking && rankingFragment != null) {
                getSupportFragmentManager().beginTransaction().show(rankingFragment).hide(homeFragment).hide(bookFragment).hide(myInfoFragment).commit();
                return true;
            } else if (id == R.id.nav_book && bookFragment != null) {
                getSupportFragmentManager().beginTransaction().show(bookFragment).hide(homeFragment).hide(rankingFragment).hide(myInfoFragment).commit();
                bookFragment.refreshDexData();
                return true;
            } else if (id == R.id.nav_my_info && myInfoFragment != null) {
                getSupportFragmentManager().beginTransaction().show(myInfoFragment).hide(homeFragment).hide(rankingFragment).hide(bookFragment).commit();
                return true;
            }
            return false;
        });
    }

    public void checkPermissionsAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            Log.d("BLE_SCAN", "권한 요청: 위치/BLE 스캔/BLE 연결");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 100);
        } else {
            Log.d("BLE_SCAN", "권한 확인 완료: 자동 BLE 스캔은 시작하지 않음");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            boolean allGranted = grantResults.length >= 3;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.d("BLE_SCAN", "권한 허용 완료: 자동 BLE 스캔은 시작하지 않음");
            } else {
                Log.d("BLE_SCAN", "BLE 스캔 시작 실패: 필수 권한 거부됨");
            }
        }
    }
    public HomeFragment getHomeFragment() {
        return homeFragment;
    }
}
