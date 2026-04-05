package com.mathlit.backend.util;

public class XpCalculator {

    private static final int[] LEVEL_THRESHOLDS = {
        0, 100, 250, 500, 900, 1400, 2000, 2800, 3800, 5000,
        6500, 8200, 10200, 12500, 15000
    };

    public static int calculateXp(int correctAnswers, String gameMode) {
        int base = correctAnswers * 10;
        if (gameMode == null) return base;
        return switch (gameMode.toUpperCase()) {
            case "SPEED_ROUND"     -> (int) (base * 1.5);
            case "SURVIVAL"        -> (int) (base * 2.0);
            case "TIMED"           -> (int) (base * 1.3);
            case "DAILY_CHALLENGE" -> (int) (base * 1.2);
            default                -> base;
        };
    }

    public static int getLevel(int totalXp) {
        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (totalXp >= LEVEL_THRESHOLDS[i]) return i + 1;
        }
        return 1;
    }
}
