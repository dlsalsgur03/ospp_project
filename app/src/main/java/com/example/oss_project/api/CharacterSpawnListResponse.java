package com.example.oss_project.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CharacterSpawnListResponse {

    @SerializedName("spawns")
    private List<SpawnItem> spawns;

    public List<SpawnItem> getSpawns() {
        return spawns;
    }

    public void setSpawns(List<SpawnItem> spawns) {
        this.spawns = spawns;
    }

    public static class SpawnItem {

        @SerializedName("spawnId")
        private Long spawnId;

        @SerializedName("sensorId")
        private Long sensorId;

        @SerializedName("sensorName")
        private String sensorName;

        @SerializedName("characterId")
        private Long characterId;

        @SerializedName("characterName")
        private String characterName;

        @SerializedName("rarity")
        private String rarity;

        @SerializedName("spawnedAt")
        private String spawnedAt;

        @SerializedName("expiresAt")
        private String expiresAt;

        public Long getSpawnId() {
            return spawnId;
        }

        public void setSpawnId(Long spawnId) {
            this.spawnId = spawnId;
        }

        public Long getSensorId() {
            return sensorId;
        }

        public void setSensorId(Long sensorId) {
            this.sensorId = sensorId;
        }

        public String getSensorName() {
            return sensorName;
        }

        public void setSensorName(String sensorName) {
            this.sensorName = sensorName;
        }

        public Long getCharacterId() {
            return characterId;
        }

        public void setCharacterId(Long characterId) {
            this.characterId = characterId;
        }

        public String getCharacterName() {
            return characterName;
        }

        public void setCharacterName(String characterName) {
            this.characterName = characterName;
        }

        public String getRarity() {
            return rarity;
        }

        public void setRarity(String rarity) {
            this.rarity = rarity;
        }

        public String getSpawnedAt() {
            return spawnedAt;
        }

        public void setSpawnedAt(String spawnedAt) {
            this.spawnedAt = spawnedAt;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}