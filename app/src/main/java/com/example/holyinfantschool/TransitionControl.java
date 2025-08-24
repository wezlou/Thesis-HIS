package com.example.holyinfantschool;

public class TransitionControl {

    private static boolean shouldGoToSplash = true;  // By default, go to SplashColorActivity

    // Setter to control whether we go to SplashColorActivity
    public static void setShouldGoToSplash(boolean shouldGoToSplash) {
        TransitionControl.shouldGoToSplash = shouldGoToSplash;
    }

    // Getter to check whether we should go to SplashColorActivity
    public static boolean shouldGoToSplash() {
        return shouldGoToSplash;
    }
}
