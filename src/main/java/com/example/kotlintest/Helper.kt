package com.example.kotlintest

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.DragEvent
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt


object HelperConstant {
    var debug = true
}

public object UiUtil {
    fun Context.toast(msg: Any?) =
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    fun Activity.fullScreenActivity() {
        window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            }
            window.decorView.systemUiVisibility = FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            //statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK != Configuration.UI_MODE_NIGHT_NO)
                    isNavigationBarContrastEnforced = false
            }
        }
    }

    fun getDomFromBg(bitmap: Bitmap, default: Int) =
        Palette.Builder(bitmap).generate().getDominantColor(default)

    fun View.quickSnack(msg: String) {
        val bg: Bitmap = getBitmapFromBg()

        val s = Snackbar.make(this, msg, Snackbar.LENGTH_SHORT)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
        val def = this.context.getColor(R.color.colorPrimary)

        val colorOne: Int = this.context.resolveThemeAttr(android.R.attr.colorBackground).data
        val colorTwo = getDomFromBg(bg, def)
        /*builder.generate().dominantSwatch?.let {
            prim = it.rgb
        } ?: run {
            prim = builder.generate().getLightVibrantColor(def)
        }*/

        s.view.setOnTouchListener { view, mot ->
            view.performClick()
            if (mot.action == DragEvent.ACTION_DRAG_STARTED)
                s.dismiss()
            true
        }
        s.setBackgroundTint(ColorUtils.blendARGB(colorOne, colorTwo, .5f))
        s.setTextColor(this.context.resolveColorAttr(android.R.attr.textColorPrimary))
        s.show()
        bg.recycle()
    }

    private fun View.getBitmapFromBg(): Bitmap {
        return when (background) {
            is VectorDrawable, is BitmapDrawable, is GradientDrawable -> background.toBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            )
            else -> let {
                val b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                background?.let {
                    val m = background as ColorDrawable
                    m.color
                    Canvas(b).drawColor(m.color)
                } ?: run {
                    Canvas(b).drawColor(context.getColor(R.color.colorPrimary))
                }
                b
            }
        }
    }

    @ColorInt
    fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
        val resolvedAttr = resolveThemeAttr(colorAttr)
        // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
        val colorRes =
            if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
        return ContextCompat.getColor(this, colorRes)
    }

    fun Context.resolveThemeAttr(@AttrRes attrRes: Int): TypedValue {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue
    }

    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red: Int = Color.red(color)
        val green: Int = Color.green(color)
        val blue: Int = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}


fun logit(msg: Any?) {
    if (HelperConstant.debug) {
        val trace: StackTraceElement? = Thread.currentThread().stackTrace[3]
        val lineNumber = trace?.lineNumber
        val methodName = trace?.methodName
        val className = trace?.fileName?.replaceAfter(".", "")?.replace(".", "")
        Log.d("Line $lineNumber", "↓↓↓  $className::$methodName()  ↓↓↓")
        Log.e("MSG", "$msg")
    }
}


fun Int.misc(int: Int, int2: Int): Int {
    (this + int + int2).run {
        return this
    }
}