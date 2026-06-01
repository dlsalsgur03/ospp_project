package com.example.oss_project;

import android.content.Context;
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
    private Context context;

    public void setItems(List<CharacterDexItem> newItems) {
        this.items = (newItems != null) ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_character, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        CharacterDexItem item = items.get(position);

        int charResId = getCharacterDrawableId(item.getCharacterId());

        if (item.isCollected()) {
            // 수집 완료 상태 (true)
            holder.tvName.setText(item.getCharacterName());
            holder.tvName.setTextColor(Color.parseColor("#333333"));
            holder.overlay.setVisibility(View.GONE);
            holder.imgLock.setVisibility(View.GONE);
            holder.imgChar.setAlpha(1.0f);
            holder.imgChar.setImageResource(charResId);
        } else {
            // 미획득 상태 (false)
            holder.tvName.setText("???");
            holder.tvName.setTextColor(Color.parseColor("#BBBBBB"));
            holder.overlay.setVisibility(View.VISIBLE);
            holder.imgLock.setVisibility(View.VISIBLE);
            holder.imgChar.setAlpha(0.3f); // 이미지를 흐리게
            holder.imgChar.setImageResource(charResId);
        }
    }

    /**
     * 캐릭터 ID에 따른 드로어블 리소스 매핑
     * 1~6: 일반 (ch1 ~ ch6)
     * 7~12: 은색 (char1_sv ~ char6_sv)
     * 13~18: 금색 (char1_gd ~ char6_gd)
     */
    private int getCharacterDrawableId(int characterId) {
        String resName;
        if (characterId >= 1 && characterId <= 6) {
            resName = "ch" + characterId;
        } else if (characterId >= 7 && characterId <= 12) {
            resName = "char" + (characterId - 6) + "_sv";
        } else if (characterId >= 13 && characterId <= 18) {
            resName = "char" + (characterId - 12) + "_gd";
        } else {
            return R.drawable.sensor_1;
        }

        int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        
        if (resId == 0) {
            // 파일을 못 찾았을 때 로그 출력
            android.util.Log.e("BookAdapter", "이미지 파일을 찾을 수 없음: " + resName);
            return R.drawable.sensor_1;
        }
        
        return resId;
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
