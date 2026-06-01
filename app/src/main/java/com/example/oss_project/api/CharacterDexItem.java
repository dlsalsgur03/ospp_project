package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CharacterDexItem {
    @Expose
    @SerializedName("characterId")
    private int characterId;

    @Expose
    @SerializedName("characterName")
    private String characterName;

    @Expose
    @SerializedName("rarity")
    private String rarity;

    @Expose
    @SerializedName("description")
    private String description;

    @Expose
    @SerializedName("baseSpawnRate")
    private double baseSpawnRate;

    @Expose
    @SerializedName("collected")
    private boolean collected;

    @Expose
    @SerializedName("collectedCount")
    private int collectedCount;

    @Expose
    @SerializedName("firstFoundAt")
    private String firstFoundAt;

    public int getCharacterId() { return characterId; }
    public String getCharacterName() { return characterName; }
    public String getRarity() { return rarity; }
    public String getDescription() { return description; }
    public double getBaseSpawnRate() { return baseSpawnRate; }
    public boolean isCollected() { return collected; }
    public int getCollectedCount() { return collectedCount; }
    public String getFirstFoundAt() { return firstFoundAt; }
}
