package com.example.oss_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss_project.api.SubmissionItemResponse;

import java.util.List;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.ViewHolder> {

    private final List<SubmissionItemResponse> items;

    public SubmissionAdapter(List<SubmissionItemResponse> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_submission, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubmissionItemResponse item = items.get(position);

        holder.tvSensorName.setText(item.sensorName != null ? item.sensorName : "-");
        holder.tvLocationName.setText(item.locationName != null ? item.locationName : "-");
        holder.tvTemperature.setText("온도 " + (item.temperature != null ? item.temperature : "-") + "°C");
        holder.tvHumidity.setText("습도 " + (item.humidity != null ? item.humidity : "-") + "%");
        holder.tvEco2.setText("eCO₂ " + (item.eco2 != null ? item.eco2 : "-"));
        holder.tvAirQuality.setText("AQI " + (item.airQuality != null ? item.airQuality : "-"));

        if (item.rewardExp != null && item.rewardExp > 0) {
            holder.tvRewardExp.setText("+" + item.rewardExp + " EXP");
        } else {
            holder.tvRewardExp.setText("EXP 미지급");
        }

        String at = item.submittedAt != null ? item.submittedAt.replace("T", " ") : "-";
        if (at.length() > 16) at = at.substring(0, 16);
        holder.tvSubmittedAt.setText(at);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSensorName, tvLocationName, tvTemperature, tvHumidity, tvEco2, tvAirQuality, tvRewardExp, tvSubmittedAt;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSensorName = itemView.findViewById(R.id.tv_sensor_name);
            tvLocationName = itemView.findViewById(R.id.tv_location_name);
            tvTemperature = itemView.findViewById(R.id.tv_temperature);
            tvHumidity = itemView.findViewById(R.id.tv_humidity);
            tvEco2 = itemView.findViewById(R.id.tv_eco2);
            tvAirQuality = itemView.findViewById(R.id.tv_air_quality);
            tvRewardExp = itemView.findViewById(R.id.tv_reward_exp);
            tvSubmittedAt = itemView.findViewById(R.id.tv_submitted_at);
        }
    }
}
