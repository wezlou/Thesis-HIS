package com.example.holyinfantschool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuestionBank {

    // Master pool of all color buttons
    private static final List<Integer> ALL_COLORS = Arrays.asList(
            R.drawable.img_34,     // red
            R.drawable.img_38,     // blue
            R.drawable.btnmango,   //yellow
            R.drawable.img_33,     //pink
            R.drawable.img_66,   // green
            R.drawable.purplebtn,  // purple
            R.drawable.orangebtn,  // orange
            R.drawable.brownbtn    // brown
    );

    public static List<Question> getQuestions() {
        List<Question> questions = new ArrayList<>();
        Random random = new Random();

        // Build each question with dynamic wrong answers
        questions.add(makeQuestion(1, "What color is the apple?", R.drawable.redapple, R.drawable.img_34));
        questions.add(makeQuestion(2, "What color is the banana?", R.drawable.banana, R.drawable.btnmango));
        questions.add(makeQuestion(3, "What color are the grapes?", R.drawable.grapes, R.drawable.purplebtn));
        questions.add(makeQuestion(4, "What color is the orange?", R.drawable.orange, R.drawable.orangebtn));
        questions.add(makeQuestion(5, "What color is the watermelon?", R.drawable.watermelon, R.drawable.img_66));
        questions.add(makeQuestion(6, "What color is the blueberry?", R.drawable.blueberry, R.drawable.img_38));
        questions.add(makeQuestion(7, "What color is the lemon?", R.drawable.lemon, R.drawable.btnmango));
        questions.add(makeQuestion(8, "What color is the strawberry?", R.drawable.strawberry, R.drawable.img_34));
        questions.add(makeQuestion(9, "What color is the pear?", R.drawable.pear, R.drawable.img_66));
        questions.add(makeQuestion(10, "What color is the eggplant?", R.drawable.eggplant, R.drawable.purplebtn));
        questions.add(makeQuestion(11, "What color is the cherry?", R.drawable.cherry, R.drawable.img_34));
        questions.add(makeQuestion(12, "What color is the mango?", R.drawable.mango, R.drawable.btnmango));
        questions.add(makeQuestion(13, "What color is the coconut?", R.drawable.coconut, R.drawable.brownbtn));
        questions.add(makeQuestion(14, "What color is the kiwi?", R.drawable.kiwi, R.drawable.img_66));
        questions.add(makeQuestion(15, "What color is the dragon fruit?", R.drawable.dragonfruit, R.drawable.img_33));

        // Shuffle questions order every game
        Collections.shuffle(questions, random);

        return questions;
    }

    private static Question makeQuestion(int id, String text, int imageRes, int correctColor) {
        Random random = new Random();
        List<Question.Answer> answers = new ArrayList<>();

        // Always add the correct answer
        answers.add(new Question.Answer(correctColor, true));

        // Build pool of wrong answers (exclude the correct one)
        List<Integer> wrongPool = new ArrayList<>(ALL_COLORS);
        wrongPool.remove(Integer.valueOf(correctColor));
        Collections.shuffle(wrongPool, random);

        // Pick first 2 wrong colors
        answers.add(new Question.Answer(wrongPool.get(0), false));
        answers.add(new Question.Answer(wrongPool.get(1), false));

        return new Question(id, text, imageRes, answers);
    }
}
