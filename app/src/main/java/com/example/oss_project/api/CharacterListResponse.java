package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CharacterListResponse {
    @Expose @SerializedName("characters") public List<CharacterItem> characters;

    public static class CharacterItem {
        @Expose @SerializedName("characterId") public Long characterId;
        @Expose @SerializedName("name") public String name;
        @Expose @SerializedName("rarity") public String rarity;
        @Expose @SerializedName("description") public String description;
        @Expose @SerializedName("baseSpawnRate") public Double baseSpawnRate;
    }
}
