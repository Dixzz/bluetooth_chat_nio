package com.example.kotlintest;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.transition.TransitionManager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import static com.example.kotlintest.HelperKt.logit;

public class Test extends Fragment {
    private static final String TAG = "Test";
    private Handler handler = new Handler();

     public static Test newInstance() {

        Bundle args = new Bundle();

        Test fragment = new Test();
        fragment.setArguments(args);
        return fragment;
    }

    private void doBounceAnimationIn(LinearLayout rootParent) {
        ObjectAnimator moveY = ObjectAnimator.ofFloat(rootParent, View.TRANSLATION_Y, dpToPx(rootParent.getContext(), -58), 0f);
        moveY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator it) {
                if (((Float) it.getAnimatedValue()) == 0f) {
                    TransitionManager.beginDelayedTransition(rootParent);
                    rootParent.getChildAt(0).setVisibility(View.VISIBLE);
                }
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

    private void doBounceAnimationOut(LinearLayout rootParent) {
        TransitionManager.beginDelayedTransition(rootParent);
        rootParent.getChildAt(0).setVisibility(View.GONE);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(rootParent, View.TRANSLATION_Y, 0f, dpToPx(rootParent.getContext(), -58));
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
        moveY.setInterpolator(new OvershootInterpolator());
        moveY.setDuration(250);
        moveY.start();
    }

    private static Bitmap blurImage(Context context, Bitmap inputBitmap) {
        float BLUR_RADIUS = 25f;

        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        tmpOut.destroy();
        tmpIn.destroy();
        theIntrinsic.destroy();
        rs.finish();

        return outputBitmap;
    }

    private void setBackgroundToInCallUi(Drawable drawable) {
        Bitmap bitmap = blurImage(getContext(), ((BitmapDrawable) drawable).getBitmap());
        if (avatarImageView.getDrawable() instanceof BitmapDrawable)
            avatarImageView.setImageBitmap(blurImage(getContext(), ((BitmapDrawable) avatarImageView.getDrawable()).getBitmap()));
        avatarImageView.setImageBitmap(bitmap);
        actualImage.setImageBitmap(bitmap);
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(() -> {
            avatarImageView.animate().alpha(1f).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(1500).start();
        }, 2000);
        if (wallpaperManager != null) {
            if (wallpaperManager.getWallpaperInfo() == null) {
                setBackgroundToInCallUi(wallpaperManager.getDrawable());
            } else {
                setBackgroundToInCallUi(wallpaperManager.getWallpaperInfo().loadThumbnail(getActivity().getPackageManager()));
            }
        }
    }

    private WallpaperManager wallpaperManager;
    private ImageView actualImage, avatarImageView;
    LottieAnimationView incallOpAnim;

    public void startCircularReveal(View view) {
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;
        getActivity().runOnUiThread(() -> {
            getActivity().getWindow().getDecorView().setBackgroundColor(Color.DKGRAY);
        });
        int finalRadius = Math.max(cx, cy);
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                getActivity().getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
                incallOpAnim.animate().scaleY(1f).scaleX(1f).setDuration(1000).setInterpolator(new LinearOutSlowInInterpolator()).start();
                incallOpAnim.playAnimation();
                view.findViewById(R.id.incall_ui_container).animate().alpha(.25f).setDuration(1000).setInterpolator(new LinearOutSlowInInterpolator()).start();
                actualImage.animate().alpha(1f).setDuration(1000).setInterpolator(new AnticipateOvershootInterpolator()).start();
                new Handler().postDelayed(() -> {
                    incallOpAnim.animate().scaleY(0f).scaleX(0f).setDuration(1000).setInterpolator(new LinearOutSlowInInterpolator()).start();
                    view.findViewById(R.id.incall_ui_container).animate().alpha(1).setDuration(1000).setInterpolator(new LinearOutSlowInInterpolator()).start();
                    actualImage.animate().alpha(.9f).setDuration(1000).setInterpolator(new DecelerateInterpolator()).start();
                    incallOpAnim.cancelAnimation();
                    timer.cancel();
                }, 5000);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(250);
        anim.start();
    }

    Timer timer = new Timer();
    TimerTask timerTask;

    CollapsingToolbarLayout collapsingToolbarLayout;

    private int adjustAlpha(int color, float factor) {
        int alpha = (int) (Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private int applyAlpha(int color, float alpha) {
        return adjustAlpha(color, alpha);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (wallpaperManager == null)
            wallpaperManager = WallpaperManager.getInstance(getContext());
        Window window = getActivity().getWindow();
        window.setBackgroundDrawable(null);
        View v = inflater.inflate(R.layout.fragment, container, false);

        int top = adjustAlpha(getContext().getColor(R.color.green), .5f);
        int mid = adjustAlpha(getContext().getColor(R.color.green), .2f);
        int bottom = Color.TRANSPARENT;
        ValueAnimator colorAnimation = ValueAnimator.ofArgb(top, getContext().getColor(R.color.green));
        colorAnimation.addUpdateListener(valueAnimator -> {
            GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Integer.parseInt(valueAnimator.getAnimatedValue().toString()), mid, bottom});
            window.setBackgroundDrawable(drawable);
        });
        v.findViewById(R.id.wew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBounceAnimationIn(v.findViewById(R.id.ticker_comeback));
            }
        });

        colorAnimation.setDuration(5000);
        colorAnimation.setRepeatCount(-1);
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.setInterpolator(new DecelerateInterpolator());
        colorAnimation.start();

        //getActivity().setTheme(R.style.InCallScreen);
        actualImage = v.findViewById(R.id.actualImage);
        //window.setStatusBarColor(Color.TRANSPARENT);
        collapsingToolbarLayout = v.findViewById(R.id.incall_ui_container);
//        collapsingToolbarLayout.setTitle();
        incallOpAnim = v.findViewById(R.id.lottieOp);
        incallOpAnim.setAnimation("abc.json");
        logit(getActivity().getDrawable(R.drawable.avd_anim));
        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(getContext(), R.drawable.avd_anim);
        /*animatedVectorDrawable.setTintList(ColorStateList.valueOf(Color.RED));
        animatedVectorDrawable.start();*/
        //actualImage.setImageDrawable(animatedVectorDrawable);
        //animatedVectorDrawable.start();
        animatedVectorDrawable.registerAnimationCallback(new Animatable2.AnimationCallback() {

            @Override
            public void onAnimationEnd(Drawable drawable) {
                super.onAnimationEnd(drawable);
                animatedVectorDrawable.start();
            }
        });

        incallOpAnim.setRepeatMode(LottieDrawable.RESTART);
        incallOpAnim.setRepeatCount(-1);
        incallOpAnim.playAnimation();
        timerTask = new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                logit(i);
                getActivity().runOnUiThread(() -> {
                    if (i == 10) {
                        runLottiePlayAnim();
                    }
                    if (i > 20) {
                        stopLottieAnim(timer);
                    }
                });
                i += 1;
            }
        };

        timer.schedule(timerTask, 1000, 1000);
        /*window.setNavigationBarColor(Color.TRANSPARENT);
        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setNavigationBarContrastEnforced(false);
        window.setNavigationBarContrastEnforced(false);*/
        //window.setDecorFitsSystemWindows(false);
        actualImage = v.findViewById(R.id.actualImage);
        avatarImageView = v.findViewById(R.id.dummyImage);
        window.getDecorView().setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        /*v.addOnAttachStateChangeListener(
                new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                        View container = v.findViewById(R.id.incall_ui_container);
                        int topInset = v.getRootWindowInsets().getSystemWindowInsetTop();
                        int bottomInset = v.getRootWindowInsets().getSystemWindowInsetBottom();
                        if (topInset != container.getPaddingTop()) {
                            TransitionManager.beginDelayedTransition(((ViewGroup) container.getParent()));
                            container.setPadding(0, topInset, 0, bottomInset);
                        }
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                    }
                });
        return v;*/
        //return super.onCreateView(inflater, container, savedInstanceState);
        return v;
    }

    private void stopLottieAnim(Timer timer) {
        getActivity().runOnUiThread(() -> {
            incallOpAnim.animate().scaleY(0f).scaleX(0f).setDuration(1000).setInterpolator(new LinearOutSlowInInterpolator()).start();
            //view.findViewById(R.id.incall_ui_container).animate().alpha(1).setDuration(1000).setInterpolator(new LinearOutSlowInInterpolator()).start();
            avatarImageView.animate().alpha(.9f).setDuration(1000).setInterpolator(new LinearOutSlowInInterpolator()).start();
            incallOpAnim.cancelAnimation();
            timer.cancel();
        });
    }

    private void runLottiePlayAnim() {
        getActivity().runOnUiThread(() -> {
            incallOpAnim.animate().scaleY(1f).scaleX(1f).setDuration(1000).setInterpolator(new LinearOutSlowInInterpolator()).start();
            incallOpAnim.playAnimation();
            //view.findViewById(R.id.incall_ui_container).animate().alpha(.25f).setDuration(1000).setInterpolator(new LinearOutSlowInInterpolator()).start();
            avatarImageView.animate().alpha(1f).setDuration(1000).setInterpolator(new LinearOutSlowInInterpolator()).start();
        });
    }

    public static void run() {

        MainActivity2 mainActivity = new MainActivity2();
        logit("Hey");
        //HelperConstant.INSTANCE.setDebug(false);
        List<Callable<Void>> list = new ArrayList<>();
        list.add(new Callable<Void>() {
            @Override
            public Void call() {

                return null;
            }
        });
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {

        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setStatusBarColor(Color.TRANSPARENT);
        win.setAttributes(winParams);
    }
}
