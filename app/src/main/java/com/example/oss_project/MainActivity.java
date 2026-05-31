package com.example.oss_project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.kakao.vectormap.KakaoMapSdk;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private RankingFragment rankingFragment;
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
            myInfoFragment = new MyInfoFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, rankingFragment, "ranking").hide(rankingFragment)
                    .add(R.id.main_container, myInfoFragment, "my_info").hide(myInfoFragment)
                    .add(R.id.main_container, homeFragment, "home")
                    .commit();
        } else {
            homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
            rankingFragment = (RankingFragment) getSupportFragmentManager().findFragmentByTag("ranking");
            myInfoFragment = (MyInfoFragment) getSupportFragmentManager().findFragmentByTag("my_info");
            
            // 복구 상황 대비: 변수가 null이면 다시 생성
            if (homeFragment == null) homeFragment = new HomeFragment();
            if (rankingFragment == null) rankingFragment = new RankingFragment();
            if (myInfoFragment == null) myInfoFragment = new MyInfoFragment();
        }

        // 하단 네비게이션 설정
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // 복구 상황 대비: 변수가 null이면 다시 찾음
            if (homeFragment == null) homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home");
            if (rankingFragment == null) rankingFragment = (RankingFragment) getSupportFragmentManager().findFragmentByTag("ranking");
            if (myInfoFragment == null) myInfoFragment = (MyInfoFragment) getSupportFragmentManager().findFragmentByTag("my_info");

            if (id == R.id.nav_map && homeFragment != null) {
                getSupportFragmentManager().beginTransaction().show(homeFragment).hide(rankingFragment).hide(myInfoFragment).commit();
                return true;
            } else if (id == R.id.nav_ranking && rankingFragment != null) {
                getSupportFragmentManager().beginTransaction().show(rankingFragment).hide(homeFragment).hide(myInfoFragment).commit();
                return true;
            } else if (id == R.id.nav_my_info && myInfoFragment != null) {
                getSupportFragmentManager().beginTransaction().show(myInfoFragment).hide(homeFragment).hide(rankingFragment).commit();
                return true;
            }
            return false;
        });
    }

    public void checkPermissionsAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 100);
        } else {
            startHomeFragmentLogic();
        }
    }

    private void startHomeFragmentLogic() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).startGpsAndScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startHomeFragmentLogic();
        }
    }
}
