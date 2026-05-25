package com.example.oss_project;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RankingActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tabPersonal;
    private TextView tabDepartment;
    private TextView tabCollege;
    private TextView tvDateRange;
    private RecyclerView rvLeaderboard;

    private LinearLayout cardRank1;
    private LinearLayout cardRank2;
    private LinearLayout cardRank3;
    private TextView tvTop1Rank;
    private TextView tvTop1Main;
    private TextView tvTop1Sub;
    private TextView tvTop1Score;

    private TextView tvTop2Rank;
    private TextView tvTop2Main;
    private TextView tvTop2Sub;
    private TextView tvTop2Score;

    private TextView tvTop3Rank;
    private TextView tvTop3Main;
    private TextView tvTop3Sub;
    private TextView tvTop3Score;

    private RankingAdapter adapter;
    private ArrayList<RankingItem> personalItems;
    private ArrayList<RankingItem> departmentItems;
    private ArrayList<RankingItem> collegeItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        initViews();
        setCurrentWeekDateRange();

        initDummyData();
        initRecyclerView();
        initClickListeners();

        selectTab(tabPersonal);
        showPersonalRanking();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tabPersonal = findViewById(R.id.tabPersonal);
        tabDepartment = findViewById(R.id.tabDepartment);
        tabCollege = findViewById(R.id.tabCollege);
        tvDateRange = findViewById(R.id.tvDateRange);
        rvLeaderboard = findViewById(R.id.rvLeaderboard);

        cardRank1 = findViewById(R.id.cardRank1);
        cardRank2 = findViewById(R.id.cardRank2);
        cardRank3 = findViewById(R.id.cardRank3);

        tvTop1Rank = findViewById(R.id.tvTop1Rank);
        tvTop1Main = findViewById(R.id.tvTop1Main);
        tvTop1Sub = findViewById(R.id.tvTop1Sub);
        tvTop1Score = findViewById(R.id.tvTop1Score);

        tvTop2Rank = findViewById(R.id.tvTop2Rank);
        tvTop2Main = findViewById(R.id.tvTop2Main);
        tvTop2Sub = findViewById(R.id.tvTop2Sub);
        tvTop2Score = findViewById(R.id.tvTop2Score);

        tvTop3Rank = findViewById(R.id.tvTop3Rank);
        tvTop3Main = findViewById(R.id.tvTop3Main);
        tvTop3Sub = findViewById(R.id.tvTop3Sub);
        tvTop3Score = findViewById(R.id.tvTop3Score);
    }

    private void setCurrentWeekDateRange() {
        Calendar calendar = Calendar.getInstance();

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int diffToMonday;
        if (dayOfWeek == Calendar.SUNDAY) {
            diffToMonday = -6;
        } else {
            diffToMonday = Calendar.MONDAY - dayOfWeek;
        }

        Calendar monday = (Calendar) calendar.clone();
        monday.add(Calendar.DAY_OF_MONTH, diffToMonday);

        Calendar sunday = (Calendar) monday.clone();
        sunday.add(Calendar.DAY_OF_MONTH, 6);

        SimpleDateFormat formatter = new SimpleDateFormat("MM.dd", Locale.KOREA);

        String dateRange = String.format(formatter.format(monday.getTime()) + " - " + formatter.format(sunday.getTime()));

        tvDateRange.setText(dateRange);
    }

    private void initDummyData() {
        personalItems = new ArrayList<>();
        personalItems.add(new RankingItem(1, "센서마스터", "전자정보대학 · 컴퓨터공학과 · Lv.6", "620 EXP"));
        personalItems.add(new RankingItem(2, "충북이", "전자정보대학 · 컴퓨터공학과 · Lv.4", "330 EXP"));
        personalItems.add(new RankingItem(3, "자바왕", "전자정보대학 · 소프트웨어학부 · Lv.4", "290 EXP"));
        personalItems.add(new RankingItem(4, "알고리즘고수", "공과대학 · 기계공학과 · Lv.3", "240 EXP"));
        personalItems.add(new RankingItem(5, "프론트담당", "전자정보대학 · 컴퓨터공학과 · Lv.3", "210 EXP"));
        personalItems.add(new RankingItem(6, "백엔드친구", "전자정보대학 · 정보통신공학부 · Lv.3", "190 EXP"));
        personalItems.add(new RankingItem(7, "기록왕", "인문대학 · 국어국문학과 · Lv.2", "150 EXP"));
        personalItems.add(new RankingItem(8, "운동중", "자연과학대학 · 수학과 · Lv.2", "130 EXP"));
        personalItems.add(new RankingItem(9, "헬스장출석", "전자정보대학 · 컴퓨터공학과 · Lv.2", "120 EXP"));
        personalItems.add(new RankingItem(10, "신입회원", "공과대학 · 전기공학과 · Lv.1", "80 EXP"));

        departmentItems = new ArrayList<>();
        departmentItems.add(new RankingItem(1, "컴퓨터공학과", "제출 120회 · 참여 18명", "1600 EXP"));
        departmentItems.add(new RankingItem(2, "소프트웨어학부", "제출 95회 · 참여 14명", "1300 EXP"));
        departmentItems.add(new RankingItem(3, "정보통신공학부", "제출 83회 · 참여 12명", "1100 EXP"));
        departmentItems.add(new RankingItem(4, "전자공학부", "제출 70회 · 참여 10명", "950 EXP"));
        departmentItems.add(new RankingItem(5, "지능로봇공학과", "제출 52회 · 참여 8명", "730 EXP"));

        collegeItems = new ArrayList<>();
        collegeItems.add(new RankingItem(1, "전자정보대학", "제출 240회 · 참여 35명", "3200 EXP"));
        collegeItems.add(new RankingItem(2, "공과대학", "제출 198회 · 참여 28명", "2760 EXP"));
        collegeItems.add(new RankingItem(3, "자연과학대학", "제출 150회 · 참여 20명", "2100 EXP"));
        collegeItems.add(new RankingItem(4, "인문대학", "제출 110회 · 참여 16명", "1600 EXP"));
        collegeItems.add(new RankingItem(5, "사회과학대학", "제출 95회 · 참여 13명", "1300 EXP"));
    }

    private void initRecyclerView() {
        adapter = new RankingAdapter(personalItems);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);
    }

    private void initClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        tabPersonal.setOnClickListener(v -> {
            selectTab(tabPersonal);
            showPersonalRanking();
        });

        tabDepartment.setOnClickListener(v -> {
            selectTab(tabDepartment);
            showDepartmentRanking();
        });

        tabCollege.setOnClickListener(v -> {
            selectTab(tabCollege);
            showCollegeRanking();
        });
    }

    private void updateTopThreeAndList(ArrayList<RankingItem> originalItems) {
        ArrayList<RankingItem> top100Items = limitToTop100(originalItems);

        bindTopCard(cardRank1, tvTop1Rank, tvTop1Main, tvTop1Sub, tvTop1Score, top100Items, 0);
        bindTopCard(cardRank2, tvTop2Rank, tvTop2Main, tvTop2Sub, tvTop2Score, top100Items, 1);
        bindTopCard(cardRank3, tvTop3Rank, tvTop3Main, tvTop3Sub, tvTop3Score, top100Items, 2);

        ArrayList<RankingItem> remainingItems = new ArrayList<>();

        if (top100Items.size() > 3) {
            remainingItems.addAll(top100Items.subList(3, top100Items.size()));
        }

        adapter.submitList(remainingItems);
    }

    private void bindTopCard(
            LinearLayout card,
            TextView tvRank,
            TextView tvMain,
            TextView tvSub,
            TextView tvScore,
            ArrayList<RankingItem> items,
            int index
    ) {
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

    private void showPersonalRanking() {
        updateTopThreeAndList(personalItems);
    }

    private void showDepartmentRanking() {
        updateTopThreeAndList(departmentItems);
    }

    private void showCollegeRanking() {
        updateTopThreeAndList(collegeItems);
    }

    private ArrayList<RankingItem> limitToTop100(ArrayList<RankingItem> originalItems) {
        int endIndex = Math.min(originalItems.size(), 100);
        return new ArrayList<>(originalItems.subList(0, endIndex));
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