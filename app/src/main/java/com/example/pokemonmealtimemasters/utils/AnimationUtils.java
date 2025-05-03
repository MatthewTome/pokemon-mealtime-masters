package com.example.pokemonmealtimemasters.utils;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Various UI Animation effects.
 */
public class AnimationUtils {

    // Simple press down and up animation
    public static void applyPressAnimation(View view) {
        if (view == null) return;
        view.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start())
                .start();
    }

    // Pop-in effect
    public static void applyPopInAnimation(View view) {
        if (view == null) return;
        view.setScaleX(0.5f);
        view.setScaleY(0.5f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    // Gentle bounce effect
    public static void applyBounceAnimation(View view) {
        if (view == null) return;
        ObjectAnimator bounce = ObjectAnimator.ofFloat(view, "translationY", 0f, -30f, 0f);
        bounce.setInterpolator(new BounceInterpolator());
        bounce.setDuration(600);
        bounce.start();
    }

    // Fade-in animation
    public static void applyFadeInAnimation(View view, long duration) {
        if (view == null) return;
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    // Fade-out animation
    public static void applyFadeOutAnimation(View view, long duration, Runnable endAction) {
        if (view == null) return;
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(endAction)
                .start();
    }

    // Dialog entrance animation (slide up and fade in)
    public static void applyDialogEntranceAnimation(View dialogView) {
        if (dialogView == null) return;
        dialogView.setTranslationY(100f);
        dialogView.setAlpha(0f);
        dialogView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(350)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    // Applies a shake animation horizontally to indicate an error or invalid input.
    public static void applyShakeAnimation(View view) {
        if (view == null) return;
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f);
        shake.setDuration(500);
        shake.start();
    }
}