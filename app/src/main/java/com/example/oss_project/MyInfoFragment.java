package com.example.oss_project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.LevelInfoResponse;
import com.example.oss_project.api.MyRankingResponse;
import com.example.oss_project.api.RetrofitClient;
import com.example.oss_project.api.SubmissionItemResponse;
import com.example.oss_project.api.SubmissionPageResponse;
import com.example.oss_project.api.UserInfoResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyInfoFragment extends Fragment {

    private TextView tvEmail, tvNickname, tvCollege, tvDepartment, tvLevel, tvExpLabel, tvTotalSubmissionCount;
    private ProgressBar pbExp;
    private Button btnLogout, btnParticipationHistory;
    private UserInfoResponse currentUserInfo;

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
        tvTotalSubmissionCount = view.findViewById(R.id.tv_total_submission_count);
        pbExp = view.findViewById(R.id.pb_exp);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnParticipationHistory = view.findViewById(R.id.btn_participation_history);

        btnLogout.setOnClickListener(v -> handleLogout());
        btnParticipationHistory.setOnClickListener(v -> showParticipationHistoryDialog());

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
                    currentUserInfo = info;

                    tvEmail.setText(info.email);
                    tvNickname.setText(info.nickname);
                    tvCollege.setText(info.college);
                    tvDepartment.setText(info.department);

                    fetchLevelInfo(token);
                    fetchMyRanking(token);
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

    private void fetchLevelInfo(String token) {
        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.getLevelInfo("Bearer " + token).enqueue(new Callback<ApiResult<LevelInfoResponse>>() {
            @Override
            public void onResponse(Call<ApiResult<LevelInfoResponse>> call, Response<ApiResult<LevelInfoResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    LevelInfoResponse info = response.body().data;

                    tvLevel.setText("Lv. " + info.level);

                    int inLevelExp = (info.currentExp != null && info.currentLevelMinExp != null)
                            ? info.currentExp - info.currentLevelMinExp : 0;
                    int levelRange = (info.nextLevelExp != null && info.currentLevelMinExp != null)
                            ? info.nextLevelExp - info.currentLevelMinExp : 1;
                    tvExpLabel.setText("EXP " + inLevelExp + " / " + levelRange);

                    int progress = (info.progressRate != null) ? (int) Math.round(info.progressRate) : 0;
                    pbExp.setProgress(progress);
                }
            }

            @Override
            public void onFailure(Call<ApiResult<LevelInfoResponse>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "레벨 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchMyRanking(String token) {
        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.getPersonalRanking("Bearer " + token).enqueue(new Callback<ApiResult<MyRankingResponse>>() {
            @Override
            public void onResponse(Call<ApiResult<MyRankingResponse>> call, Response<ApiResult<MyRankingResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    bindMyRanking(response.body().data);
                }
            }

            @Override
            public void onFailure(Call<ApiResult<MyRankingResponse>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "랭킹 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void bindMyRanking(MyRankingResponse ranking) {
        if (currentUserInfo == null) return;

        tvNickname.setText(currentUserInfo.nickname + formatRank(ranking.overallRank));
        tvCollege.setText(currentUserInfo.college + formatRank(ranking.collegeRank));
        tvDepartment.setText(currentUserInfo.department + formatRank(ranking.departmentRank));

        if (ranking.totalSubmissionCount != null) {
            tvTotalSubmissionCount.setText("총 제출 " + ranking.totalSubmissionCount + "회");
        }
    }

    private String formatRank(Integer rank) {
        if (rank == null || rank <= 0) return "";
        return " (" + rank + "위)";
    }

    private void showParticipationHistoryDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_participation_history, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        ImageButton btnClose = dialogView.findViewById(R.id.btn_close_history);
        ProgressBar progressHistory = dialogView.findViewById(R.id.progress_history);
        RecyclerView rvSubmissions = dialogView.findViewById(R.id.rv_submissions);
        TextView tvTotalCount = dialogView.findViewById(R.id.tv_total_count);
        TextView tvEmpty = dialogView.findViewById(R.id.tv_empty);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        rvSubmissions.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences prefs = getContext().getSharedPreferences("auth_pref", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token == null) {
            progressHistory.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.getMySubmissions("Bearer " + token, 0, 50).enqueue(new Callback<ApiResult<SubmissionPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResult<SubmissionPageResponse>> call, Response<ApiResult<SubmissionPageResponse>> response) {
                if (getContext() == null) return;
                progressHistory.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    SubmissionPageResponse data = response.body().data;
                    List<SubmissionItemResponse> submissions = data.submissions;

                    tvTotalCount.setText("총 " + (data.totalElements != null ? data.totalElements : 0) + "건");

                    if (submissions == null || submissions.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvSubmissions.setVisibility(View.VISIBLE);
                        rvSubmissions.setAdapter(new SubmissionAdapter(submissions));
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "참여 내역을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResult<SubmissionPageResponse>> call, Throwable t) {
                if (getContext() == null) return;
                progressHistory.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "네트워크 에러", Toast.LENGTH_SHORT).show();
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
