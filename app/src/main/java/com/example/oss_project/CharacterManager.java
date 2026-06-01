package com.example.oss_project;

import java.util.Random;

public class CharacterManager {
    private static final int[] NORMAL_CHARS = {R.drawable.ch1, R.drawable.ch2, R.drawable.ch3, R.drawable.ch4, R.drawable.ch5, R.drawable.ch6};
    private static final int[] SILVER_CHARS = {R.drawable.char1_sv, R.drawable.char2_sv, R.drawable.char3_sv, R.drawable.char4_sv, R.drawable.char5_sv, R.drawable.char6_sv};
    private static final int[] GOLD_CHARS = {R.drawable.char1_gd, R.drawable.char2_gd, R.drawable.char3_gd, R.drawable.char4_gd, R.drawable.char5_gd, R.drawable.char6_gd};

    public static Integer generateRandomSpawn() {
        Random random = new Random();
        
        // 20% 확률로 캐릭터 출몰 여부 결정 (5개 중 1개 꼴)
        if (random.nextInt(5) != 0) return null;

        double gradeRoll = random.nextDouble() * 100;
        int subIndex = random.nextInt(6);
        
        Integer resultId;
        if (gradeRoll < 76) resultId = subIndex + 1;
        else if (gradeRoll < 94) resultId = subIndex + 7;
        else resultId = subIndex + 13;

        return resultId;
    }

    public static int getCharacterDrawableId(int characterId) {
        if (characterId >= 1 && characterId <= 6) return NORMAL_CHARS[characterId - 1];
        if (characterId >= 7 && characterId <= 12) return SILVER_CHARS[characterId - 7];
        if (characterId >= 13 && characterId <= 18) return GOLD_CHARS[characterId - 13];
        return R.drawable.sensor_1;
    }
}
