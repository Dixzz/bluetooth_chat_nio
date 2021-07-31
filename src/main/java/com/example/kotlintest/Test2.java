package com.example.kotlintest;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionManager;

import com.google.android.material.card.MaterialCardView;

public class Test2 extends Fragment {
    private static final String TAG = "Test";
    //titleTicker="", bodyTicker=""
    private Handler handler = new Handler();
    private Context context;

    private void doBounceAnimationIn(MaterialCardView rootParent) {
        ObjectAnimator moveY = ObjectAnimator.ofFloat(rootParent, View.TRANSLATION_Y, dpToPx(context, -58), 0f);
        moveY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.d(TAG, "onAnimationEnd: " + (rootParent.findViewById(R.id.childOne).getVisibility() == View.VISIBLE));
                TransitionManager.beginDelayedTransition(rootParent);
                rootParent.findViewById(R.id.childOne).setVisibility(View.VISIBLE);
                handler.postDelayed(() -> {
                    doBounceAnimationOut(rootParent);
                }, 2000);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        moveY.setInterpolator(new OvershootInterpolator());
        moveY.setDuration(250);
        moveY.start();
    }

    public static float dpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    private void doBounceAnimationOut(MaterialCardView rootParent) {
        TransitionManager.beginDelayedTransition(rootParent);
        rootParent.findViewById(R.id.childOne).setVisibility(View.GONE);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(rootParent, View.TRANSLATION_Y, 0f, dpToPx(context, -58));
        moveY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //notificationStackScroller.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        moveY.setStartDelay(800);
        moveY.setInterpolator(new OvershootInterpolator());
        moveY.setDuration(250);
        moveY.start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        return inflater.inflate(R.layout.aaa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.wew1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBounceAnimationIn(view.findViewById(R.id.ticker_comeback));
            }
        });
    }
}