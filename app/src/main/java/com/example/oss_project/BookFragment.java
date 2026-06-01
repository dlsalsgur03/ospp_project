package com.example.oss_project;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss_project.api.ApiResult;
import com.example.oss_project.api.ApiService;
import com.example.oss_project.api.CharacterDexItem;
import com.example.oss_project.api.DexData;
import com.example.oss_project.api.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookFragment extends Fragment {

    private TextView tabCommon, tabRare, tabLegendary;
    private BookAdapter adapter;
    private List<CharacterDexItem> allItems = new ArrayList<>();
    private String currentFilter = "COMMON";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book, container, false);

        tabCommon = view.findViewById(R.id.tabCommon);
        tabRare = view.findViewById(R.id.tabRare);
        tabLegendary = view.findViewById(R.id.tabLegendary);
        RecyclerView rvBook = view.findViewById(R.id.rv_book);

        adapter = new BookAdapter();
        rvBook.setAdapter(adapter);

        setupTabs();
        fetchDexData();

        return view;
    }

    private void setupTabs() {
        View.OnClickListener tabListener = v -> {
            resetTabs();
            int id = v.getId();
            if (id == R.id.tabCommon) {
                currentFilter = "COMMON";
                selectTab(tabCommon);
            } else if (id == R.id.tabRare) {
                currentFilter = "RARE";
                selectTab(tabRare);
            } else if (id == R.id.tabLegendary) {
                currentFilter = "LEGENDARY";
                selectTab(tabLegendary);
            }
            filterList();
        };

        tabCommon.setOnClickListener(tabListener);
        tabRare.setOnClickListener(tabListener);
        tabLegendary.setOnClickListener(tabListener);
    }

    private void resetTabs() {
        tabCommon.setBackground(null);
        tabCommon.setTextColor(Color.parseColor("#191A1D"));
        tabRare.setBackground(null);
        tabRare.setTextColor(Color.parseColor("#191A1D"));
        tabLegendary.setBackground(null);
        tabLegendary.setTextColor(Color.parseColor("#191A1D"));
    }

    private void selectTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_tab_selected);
        tab.setTextColor(Color.WHITE);
    }

    private void fetchDexData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("auth_pref", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        if (token == null) {
            Log.e("BookFragment", "토큰이 없습니다.");
            return;
        }

        ApiService service = RetrofitClient.getClient().create(ApiService.class);
        service.getCharacterDex("Bearer " + token).enqueue(new Callback<ApiResult<DexData>>() {
            @Override
            public void onResponse(Call<ApiResult<DexData>> call, Response<ApiResult<DexData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    allItems = response.body().data.getCharacters();
                    Log.d("BookFragment", "데이터 수신 성공: " + (allItems != null ? allItems.size() : 0) + "개");
                    filterList();
                } else {
                    Log.e("BookFragment", "응답 실패 코드: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResult<DexData>> call, Throwable t) {
                Log.e("BookFragment", "네트워크 에러 발생: " + t.getMessage());
            }
        });
    }

    public void refreshDexData() {
        if (isAdded()) {
            fetchDexData();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshDexData();
        }
    }

    private void filterList() {
        if (allItems == null) return;

        List<CharacterDexItem> filtered = allItems.stream()
                .filter(item -> item.getRarity().equalsIgnoreCase(currentFilter))
                .collect(Collectors.toList());

        adapter.setItems(filtered);
    }
}
