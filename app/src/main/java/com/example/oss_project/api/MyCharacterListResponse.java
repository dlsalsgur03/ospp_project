package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MyCharacterListResponse {
    @Expose @SerializedName("characters") public List<MyCharacterItem> characters;

    public static class MyCharacterItem {
        @Expose @SerializedName("collectionId") public Long collectionId;
        @Expose @SerializedName("characterId") public Long characterId;
        @Expose @SerializedName("characterName") public String characterName;
        @Expose @SerializedName("rarity") public String rarity;
        @Expose @SerializedName("sensorId") public Long sensorId;
        @Expose @SerializedName("sensorName") public String sensorName;
        @Expose @SerializedName("foundAt") public String foundAt;
    }
}
