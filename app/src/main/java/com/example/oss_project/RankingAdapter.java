package com.example.oss_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.LeaderboardViewHolder> {

    private List<RankingItem> items;

    public RankingAdapter(List<RankingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        RankingItem item = items.get(position);

        holder.tvRank.setText(String.valueOf(item.getRank()));
        holder.tvMain.setText(item.getMainText());
        holder.tvSub.setText(item.getSubText());
        holder.tvScore.setText(item.getScoreText());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        TextView tvMain;
        TextView tvSub;
        TextView tvScore;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRank = itemView.findViewById(R.id.tvRank);
            tvMain = itemView.findViewById(R.id.tvMain);
            tvSub = itemView.findViewById(R.id.tvSub);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }

    public void submitList(List<RankingItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
}