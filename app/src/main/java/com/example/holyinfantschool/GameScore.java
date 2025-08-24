package com.example.holyinfantschool;

public class GameScore {
    private static int correct = 0;
    private static int incorrect = 0;

    public static void incrementCorrect() {
        correct++;
    }

    public static void incrementIncorrect() {
        incorrect++;
    }

    public static int getCorrect() {
        return correct;
    }

    public static int getIncorrect() {
        return incorrect;
    }

    public static void reset() {
        correct = 0;
        incorrect = 0;
    }
}