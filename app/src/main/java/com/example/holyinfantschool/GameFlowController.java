package com.example.holyinfantschool;

import android.app.Activity;
import android.content.Intent;

public class GameFlowController {

    public static void navigateToResult(
            Activity currentActivity,
            boolean isCorrect,
            int imageRes,
            int colorRes,
            String nextActivity,
            int nextIndex
    ) {

        if (isCorrect) {
            GameScore.incrementCorrect();
        } else {
            GameScore.incrementIncorrect();
        }

        Intent intent = new Intent(
                currentActivity,
                isCorrect ? CorrectActivity.class : IncorrectActivity.class
        );

        intent.putExtra("IMAGE_RES", imageRes);
        intent.putExtra("COLOR_RES", colorRes);
        intent.putExtra("NEXT_ACTIVITY", nextActivity);
        intent.putExtra("NEXT_INDEX", nextIndex);

        currentActivity.startActivity(intent);

        currentActivity.finish();
    }
}
