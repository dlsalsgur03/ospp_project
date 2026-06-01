package com.example.oss_project;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss_project.api.CharacterDexItem;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<CharacterDexItem> items = new ArrayList<>();

    public void setItems(List<CharacterDexItem> newItems) {
        this.items = (newItems != null) ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_character, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        CharacterDexItem item = items.get(position);

        if (item.isCollected()) {
            // 수집 완료 상태 (true)
            holder.tvName.setText(item.getCharacterName());
            holder.tvName.setTextColor(Color.parseColor("#333333"));
            holder.overlay.setVisibility(View.GONE);
            holder.imgLock.setVisibility(View.GONE);
            holder.imgChar.setAlpha(1.0f);
            
            // 임시 이미지 (추후 캐릭터 ID 매핑 필요)
            holder.imgChar.setImageResource(R.drawable.sensor_1);
        } else {
            // 미획득 상태 (false)
            holder.tvName.setText("???");
            holder.tvName.setTextColor(Color.parseColor("#BBBBBB"));
            holder.overlay.setVisibility(View.VISIBLE);
            holder.imgLock.setVisibility(View.VISIBLE);
            holder.imgChar.setAlpha(0.3f); // 이미지를 흐리게
            holder.imgChar.setImageResource(R.drawable.sensor_1);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView imgChar, imgLock;
        View overlay;
        TextView tvName;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            imgChar = itemView.findViewById(R.id.img_character);
            imgLock = itemView.findViewById(R.id.img_lock);
            overlay = itemView.findViewById(R.id.view_locked_overlay);
            tvName = itemView.findViewById(R.id.tv_character_name);
        }
    }
}
