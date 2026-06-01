package com.example.oss_project;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.CollegeRankingListResponse;
import com.example.oss_project.api.CollegeRankingResponse;
import com.example.oss_project.api.DepartmentRankingListResponse;
import com.example.oss_project.api.DepartmentRankingResponse;
import com.example.oss_project.api.RetrofitClient;
import com.example.oss_project.api.UserRankingListResponse;
import com.example.oss_project.api.UserRankingResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RankingFragment extends Fragment {

    private TextView tabPersonal, tabDepartment, tabCollege, tvDateRange;
    private RecyclerView rvLeaderboard;
    private LinearLayout cardRank1, cardRank2, cardRank3;
    private TextView tvTop1Rank, tvTop1Main, tvTop1Sub, tvTop1Score;
    private TextView tvTop2Rank, tvTop2Main, tvTop2Sub, tvTop2Score;
    private TextView tvTop3Rank, tvTop3Main, tvTop3Sub, tvTop3Score;

    private RankingAdapter adapter;
    private ArrayList<RankingItem> rankingItems = new ArrayList<>();
    private String myToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        initViews(view);
        setCurrentWeekDateRange();
        initRecyclerView();
        loadMyInfo();
        initClickListeners();

        // 초기 화면: 개인 랭킹
        selectTab(tabPersonal);
        fetchPersonalRanking();

        return view;
    }

    private void initViews(View view) {
        tabPersonal = view.findViewById(R.id.tabPersonal);
        tabDepartment = view.findViewById(R.id.tabDepartment);
        tabCollege = view.findViewById(R.id.tabCollege);
        tvDateRange = view.findViewById(R.id.tvDateRange);
        rvLeaderboard = view.findViewById(R.id.rvLeaderboard);
        cardRank1 = view.findViewById(R.id.cardRank1); cardRank2 = view.findViewById(R.id.cardRank2); cardRank3 = view.findViewById(R.id.cardRank3);
        tvTop1Rank = view.findViewById(R.id.tvTop1Rank); tvTop1Main = view.findViewById(R.id.tvTop1Main);
        tvTop1Sub = view.findViewById(R.id.tvTop1Sub); tvTop1Score = view.findViewById(R.id.tvTop1Score);
        tvTop2Rank = view.findViewById(R.id.tvTop2Rank); tvTop2Main = view.findViewById(R.id.tvTop2Main);
        tvTop2Sub = view.findViewById(R.id.tvTop2Sub); tvTop2Score = view.findViewById(R.id.tvTop2Score);
        tvTop3Rank = view.findViewById(R.id.tvTop3Rank); tvTop3Main = view.findViewById(R.id.tvTop3Main);
        tvTop3Sub = view.findViewById(R.id.tvTop3Sub); tvTop3Score = view.findViewById(R.id.tvTop3Score);
    }

    private void loadMyInfo() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("auth_pref", Context.MODE_PRIVATE);
        myToken = prefs.getString("access_token", "");
    }

    private void setCurrentWeekDateRange() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int diffToMonday = (dayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dayOfWeek;
        Calendar monday = (Calendar) calendar.clone();
        monday.add(Calendar.DAY_OF_MONTH, diffToMonday);
        Calendar sunday = (Calendar) monday.clone();
        sunday.add(Calendar.DAY_OF_MONTH, 6);
        SimpleDateFormat formatter = new SimpleDateFormat("MM.dd", Locale.KOREA);
        tvDateRange.setText(formatter.format(monday.getTime()) + " - " + formatter.format(sunday.getTime()));
    }

    private void initRecyclerView() {
        if (getContext() == null) return;
        adapter = new RankingAdapter(rankingItems);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLeaderboard.setAdapter(adapter);
    }

    private void initClickListeners() {
        tabPersonal.setOnClickListener(v -> { selectTab(tabPersonal); fetchPersonalRanking(); });
        tabDepartment.setOnClickListener(v -> { selectTab(tabDepartment); fetchDepartmentRanking(); });
        tabCollege.setOnClickListener(v -> { selectTab(tabCollege); fetchCollegeRanking(); });
    }

    private void fetchPersonalRanking() {
        if (myToken.isEmpty()) return;
        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.getUsersRanking("Bearer " + myToken).enqueue(new Callback<ApiResult<UserRankingListResponse>>() {
            @Override
            public void onResponse(Call<ApiResult<UserRankingListResponse>> call, Response<ApiResult<UserRankingListResponse>> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().data != null
                        && response.body().data.rankings != null) {
                    displayUserRanking(response.body().data.rankings);
                } else {
                    if (getContext() != null) Toast.makeText(getContext(), "개인 랭킹을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResult<UserRankingListResponse>> call, Throwable t) {
                Log.e("RankingError", "개인 랭킹 실패 원인: " + t.getMessage());
                if (getContext() != null) Toast.makeText(getContext(), "네트워크 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDepartmentRanking() {
        if (myToken.isEmpty()) return;
        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.getDepartmentRanking("Bearer " + myToken).enqueue(new Callback<ApiResult<DepartmentRankingListResponse>>() {
            @Override
            public void onResponse(Call<ApiResult<DepartmentRankingListResponse>> call, Response<ApiResult<DepartmentRankingListResponse>> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().data != null
                        && response.body().data.rankings != null) {
                    displayDepartmentRanking(response.body().data.rankings);
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "학과별 랭킹 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResult<DepartmentRankingListResponse>> call, Throwable t) {
                Log.e("RankingError", "학과별 랭킹 실패 원인: " + t.getMessage());
                if (getContext() != null) Toast.makeText(getContext(), "네트워크 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCollegeRanking() {
        if (myToken.isEmpty()) return;
        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.getCollegeRanking("Bearer " + myToken).enqueue(new Callback<ApiResult<CollegeRankingListResponse>>() {
            @Override
            public void onResponse(Call<ApiResult<CollegeRankingListResponse>> call, Response<ApiResult<CollegeRankingListResponse>> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().data != null
                        && response.body().data.rankings != null) {
                    displayCollegeRanking(response.body().data.rankings);
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "단과대별 랭킹 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResult<CollegeRankingListResponse>> call, Throwable t) {
                Log.e("RankingError", "단과대별 랭킹 실패 원인: " + t.getMessage());
                if (getContext() != null) Toast.makeText(getContext(), "네트워크 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserRanking(List<UserRankingResponse> responses) {
        rankingItems.clear();
        for (UserRankingResponse res : responses) {
            String sub = res.college + " · " + res.department + " · Lv." + res.level;
            rankingItems.add(new RankingItem(res.rank, res.nickname, sub, formatSubmissionCount(res.totalSubmissionCount)));
        }
        updateTopThreeAndList();
    }

    private void displayCollegeRanking(List<CollegeRankingResponse> responses) {
        rankingItems.clear();
        for (CollegeRankingResponse res : responses) {
            String sub = "참여 " + res.userCount + "명";
            rankingItems.add(new RankingItem(res.rank, res.college, sub, formatSubmissionCount(res.totalSubmissionCount)));
        }
        updateTopThreeAndList();
    }

    private void displayDepartmentRanking(List<DepartmentRankingResponse> responses) {
        rankingItems.clear();
        for (DepartmentRankingResponse res : responses) {
            String sub = "참여 " + res.userCount + "명";
            rankingItems.add(new RankingItem(res.rank, res.department, sub, formatSubmissionCount(res.totalSubmissionCount)));
        }
        updateTopThreeAndList();
    }

    private String formatSubmissionCount(Integer totalSubmissionCount) {
        int count = totalSubmissionCount != null ? totalSubmissionCount : 0;
        return "제출 " + count + "회";
    }

    private void updateTopThreeAndList() {
        bindTopCard(cardRank1, tvTop1Rank, tvTop1Main, tvTop1Sub, tvTop1Score, rankingItems, 0);
        bindTopCard(cardRank2, tvTop2Rank, tvTop2Main, tvTop2Sub, tvTop2Score, rankingItems, 1);
        bindTopCard(cardRank3, tvTop3Rank, tvTop3Main, tvTop3Sub, tvTop3Score, rankingItems, 2);

        ArrayList<RankingItem> remainingItems = new ArrayList<>();
        if (rankingItems.size() > 3) {
            remainingItems.addAll(rankingItems.subList(3, Math.min(rankingItems.size(), 100)));
        }
        adapter.submitList(remainingItems);
    }

    private void bindTopCard(LinearLayout card, TextView tvRank, TextView tvMain, TextView tvSub, TextView tvScore, ArrayList<RankingItem> items, int index) {
        if (items.size() > index) {
            RankingItem item = items.get(index);
            card.setVisibility(View.VISIBLE);
            tvRank.setText(item.getRank() + "위");
            tvMain.setText(item.getMainText());
            tvSub.setText(item.getSubText());
            tvScore.setText(item.getScoreText());
        } else {
            card.setVisibility(View.GONE);
        }
    }

    private void selectTab(TextView selectedTab) {
        TextView[] tabs = {tabPersonal, tabDepartment, tabCollege};
        for (TextView tab : tabs) {
            tab.setBackground(null);
            tab.setTextColor(Color.parseColor("#333333"));
        }
        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected);
        selectedTab.setTextColor(Color.WHITE);
    }
}
