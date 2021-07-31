package com.example.kotlintest;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;

public class GradientAnim extends AnimationDrawable {
    static Integer start = Color.red(255);
    static Integer center = Color.rgb(2, 106, 154);
    static Integer end = Color.rgb(28, 179, 249);
    Integer frameDuration = 3000;
    Integer enterFade = 0;
    Integer exitFade = 3000;
    static GradientDrawable gradientStart, gradientCenter, gradientEnd;

    static {
        gradientStart = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{start, center, end});
        gradientStart.setShape(GradientDrawable.RECTANGLE);
        gradientStart.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientCenter = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{center, end, start});
        gradientCenter.setShape(GradientDrawable.RECTANGLE);
        gradientCenter.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientEnd = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[]{end, start, center});
        gradientEnd.setShape(GradientDrawable.RECTANGLE);
        gradientEnd.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

    public GradientAnim() {
        super();
        addFrame(gradientStart, frameDuration);
        addFrame(gradientCenter, frameDuration);
        addFrame(gradientEnd, frameDuration);
        setEnterFadeDuration(enterFade);
        setExitFadeDuration(exitFade);
        setOneShot(false);
    }
}
