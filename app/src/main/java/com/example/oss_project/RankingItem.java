package com.example.oss_project;

import com.google.android.material.color.utilities.Score;

public class RankingItem {
    private int rank;
    private String mainText;
    private String subText;
    private String scoreText;

    public RankingItem(int rank, String mainText, String subText, String scoreText) {
        this.rank = rank;
        this.mainText = mainText;
        this.subText = subText;
        this.scoreText = scoreText;
    }

    public int getRank() {
        return rank;
    }
    public String getMainText() {
        return mainText;
    }
    public String getSubText() {
        return subText;
    }
    public String getScoreText() {
        return scoreText;
    }
}