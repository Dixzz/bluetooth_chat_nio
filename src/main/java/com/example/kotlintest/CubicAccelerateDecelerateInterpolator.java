package com.example.kotlintest;

import android.view.animation.BaseInterpolator;

public class CubicAccelerateDecelerateInterpolator extends BaseInterpolator {
    @Override
    public float getInterpolation(float t) {
        float x = t * 2.0f;
        double cube = Math.pow(x, 3);
        if (t < 0.5f) {
            return (float) (0.5f * cube);
        }
        x = (t - 0.5f) * 2 - 1;
        return (float) (0.5f * Math.pow(x, 3) + 1);
    }
}