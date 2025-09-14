package com.example.holyinfantschool;

import java.util.List;

public class GameSession {
    private static List<Question> questions;

    public static void setQuestions(List<Question> q) {
        questions = q;
    }

    public static List<Question> getQuestions() {
        return questions;
    }

    public static void reset() {
        questions = null;
    }
}
