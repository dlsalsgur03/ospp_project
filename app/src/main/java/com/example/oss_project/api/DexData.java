package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DexData {
    @Expose
    @SerializedName("totalCount")
    private int totalCount;

    @Expose
    @SerializedName("collectedCount")
    private int collectedCount;

    @Expose
    @SerializedName("characters")
    private List<CharacterDexItem> characters;

    public int getTotalCount() { return totalCount; }
    public int getCollectedCount() { return collectedCount; }
    public List<CharacterDexItem> getCharacters() {
        return characters;
    }
}
