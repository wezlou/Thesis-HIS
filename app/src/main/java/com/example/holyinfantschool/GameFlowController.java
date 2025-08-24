package com.example.holyinfantschool;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

public class GameFlowController {
    private static final int DELAY_BEFORE_SPLASH = 3000;
    private static final int SPLASH_DURATION = 3000;

    public static void navigateToResult(Activity currentActivity, boolean isCorrect, int imageRes, String nextActivity) {
        // Update score
        if (isCorrect) {
            GameScore.incrementCorrect();
        } else {
            GameScore.incrementIncorrect();
        }

        // Go to Correct/Incorrect activity
        Intent intent = new Intent(currentActivity, isCorrect ? CorrectActivity.class : IncorrectActivity.class);
        intent.putExtra("IMAGE_RES", imageRes);
        currentActivity.startActivity(intent);
        currentActivity.finish();

        // After delay, go to splash then next activity
        new Handler().postDelayed(() -> {
            Intent splashIntent = new Intent(currentActivity, SplashColorActivity.class);
            splashIntent.putExtra("NEXT_ACTIVITY", nextActivity);
            currentActivity.startActivity(splashIntent);
        }, DELAY_BEFORE_SPLASH);
    }
}