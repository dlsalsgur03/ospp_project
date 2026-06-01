package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CharacterSpawnItem {

    @Expose
    @SerializedName("spawnId")
    private Long spawnId;

    @Expose
    @SerializedName("sensorId")
    private Long sensorId;

    @Expose
    @SerializedName("sensorName")
    private String sensorName;

    @Expose
    @SerializedName("characterId")
    private Long characterId;

    @Expose
    @SerializedName("characterName")
    private String characterName;

    @Expose
    @SerializedName("rarity")
    private String rarity;

    @Expose
    @SerializedName("spawnedAt")
    private String spawnedAt;

    @Expose
    @SerializedName("expiresAt")
    private String expiresAt;

    public Long getSpawnId() {
        return spawnId;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public String getSensorName() {
        return sensorName;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getRarity() {
        return rarity;
    }

    public String getSpawnedAt() {
        return spawnedAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}