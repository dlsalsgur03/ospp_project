package com.example.oss_project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.RetrofitClient;
import com.example.oss_project.api.UserInfoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyInfoFragment extends Fragment {

    private TextView tvEmail, tvNickname, tvCollege, tvDepartment, tvLevel, tvExpLabel;
    private ProgressBar pbExp;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_info, container, false);

        tvEmail = view.findViewById(R.id.tv_my_email);
        tvNickname = view.findViewById(R.id.tv_my_nickname);
        tvCollege = view.findViewById(R.id.tv_my_college);
        tvDepartment = view.findViewById(R.id.tv_my_department);
        tvLevel = view.findViewById(R.id.tv_my_level);
        tvExpLabel = view.findViewById(R.id.tv_exp_label);
        pbExp = view.findViewById(R.id.pb_exp);
        btnLogout = view.findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(v -> handleLogout());

        fetchUserInfo();

        return view;
    }

    private void fetchUserInfo() {
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("auth_pref", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "로그인 정보가 없습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.getUserInfo("Bearer " + token).enqueue(new Callback<ApiResult<UserInfoResponse>>() {
            @Override
            public void onResponse(Call<ApiResult<UserInfoResponse>> call, Response<ApiResult<UserInfoResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    UserInfoResponse info = response.body().data;
                    
                    tvEmail.setText(info.email);
                    tvNickname.setText(info.nickname);
                    tvCollege.setText(info.college);
                    tvDepartment.setText(info.department);
                    tvLevel.setText("Lv. " + info.level);
                    
                    // EXP 설정 (1000 기준)
                    tvExpLabel.setText("EXP " + info.exp + " / 1000");
                    pbExp.setProgress(info.exp);
                    
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResult<UserInfoResponse>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "네트워크 에러", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleLogout() {
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("auth_pref", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        if (token != null) {
            ApiService service = RetrofitClient.getClient().create(ApiService.class);
            service.logout("Bearer " + token).enqueue(new Callback<ApiResult<Void>>() {
                @Override
                public void onResponse(Call<ApiResult<Void>> call, Response<ApiResult<Void>> response) {
                    performLocalLogout();
                }

                @Override
                public void onFailure(Call<ApiResult<Void>> call, Throwable t) {
                    performLocalLogout();
                }
            });
        } else {
            performLocalLogout();
        }
    }

    private void performLocalLogout() {
        if (getContext() == null) return;

        // 저장된 정보 삭제
        SharedPreferences prefs = getContext().getSharedPreferences("auth_pref", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Toast.makeText(getContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

        // 로그인 화면으로 이동
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
