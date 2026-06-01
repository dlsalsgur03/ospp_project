package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CharacterSpawnListResponse {
    @Expose @SerializedName("spawns") public List<SpawnItem> spawns;

    public static class SpawnItem {
        @Expose @SerializedName("spawnId") public Long spawnId;
        @Expose @SerializedName("sensorId") public Long sensorId;
        @Expose @SerializedName("sensorName") public String sensorName;
        @Expose @SerializedName("characterId") public Long characterId;
        @Expose @SerializedName("characterName") public String characterName;
        @Expose @SerializedName("rarity") public String rarity;
        @Expose @SerializedName("spawnedAt") public String spawnedAt;
        @Expose @SerializedName("expiresAt") public String expiresAt;
    }
}
