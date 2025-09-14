package com.example.holyinfantschool;

import android.app.Activity;
import android.content.Intent;

public class GameFlowController {

    public static void navigateToResult(Activity currentActivity,
                                        boolean isCorrect,
                                        int imageRes,
                                        int colorRes,       // NEW: pass color
                                        String nextActivity,
                                        int nextIndex) {

        // Update score
        if (isCorrect) {
            GameScore.incrementCorrect();
        } else {
            GameScore.incrementIncorrect();
        }

        // Show Correct or Incorrect activity
        Intent intent = new Intent(currentActivity,
                isCorrect ? CorrectActivity.class : IncorrectActivity.class);

        intent.putExtra("IMAGE_RES", imageRes);
        intent.putExtra("COLOR_RES", colorRes);   // send color too
        intent.putExtra("NEXT_ACTIVITY", nextActivity);
        intent.putExtra("NEXT_INDEX", nextIndex);
        currentActivity.startActivity(intent);

        // Finish current question screen immediately
        currentActivity.finish();
    }
}
