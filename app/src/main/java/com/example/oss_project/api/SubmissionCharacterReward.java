package com.example.oss_project.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubmissionCharacterReward {
    @Expose @SerializedName("characterId") public Long characterId;
    @Expose @SerializedName("characterName") public String characterName;
    @Expose @SerializedName("rarity") public String rarity;
    @Expose @SerializedName("bonusExp") public Integer bonusExp;
}
