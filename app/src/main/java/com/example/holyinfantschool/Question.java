package com.example.holyinfantschool;

import java.util.Collections;
import java.util.List;

public class Question {
    private int id;
    private String text;
    private int imageRes;
    private List<Answer> answers;

    public Question(int id, String text, int imageRes, List<Answer> answers) {
        this.id = id;
        this.text = text;
        this.imageRes = imageRes;
        this.answers = answers;
        shuffleAnswers(); // shuffle when created
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public int getImageRes() { return imageRes; }
    public List<Answer> getAnswers() { return answers; }

    public void shuffleAnswers() {
        Collections.shuffle(answers);
    }

    public static class Answer {
        private int imageRes;
        private boolean isCorrect;

        public Answer(int imageRes, boolean isCorrect) {
            this.imageRes = imageRes;
            this.isCorrect = isCorrect;
        }

        public int getImageRes() { return imageRes; }
        public boolean isCorrect() { return isCorrect; }
    }
}
