package com.example.kotlintest

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.annotation.Dimension
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import com.example.kotlintest.UiUtil.fullScreenActivity
import com.example.kotlintest.UiUtil.quickSnack
import kotlinx.android.synthetic.main.activity_main.*
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.system.measureTimeMillis


private const val TAG = "MainActivity2"

//@AndroidEntryPoint
class MainActivity2 : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        val ee = WallpaperManager.getInstance(this)
        ee.wallpaperInfo?.let {
            extractWallpaper(ee.wallpaperInfo.loadThumbnail(packageManager))
        } ?: run {
            android.R.anim.accelerate_decelerate_interpolator
            extractWallpaper(ee.drawable)
        }
    }

    private fun extractWallpaper(drawable: Drawable) {
        root.background = drawable
        /*inCallUiBgNice.setImageDrawable(drawable)
        dummy.setImageDrawable(drawable)*/
        /*blurImage(this, drawable.toBitmap())?.let {
            nice.setImageBitmap(it)
            //dummy.setImageBitmap(it)
        }*/
    }

    private fun doshit() {
        val query = "#files_list > tbody > tr:first-child > td:nth-child(2) > abbr"

        /*logit(
            Jsoup.connect("https://sourceforge.net/projects/octavi-os/files/sanderas/").get()
                .select(query).text()
        )*/
    }

    fun humanReadableByteCountBin(bytes: Long): String {
        val absB =
            if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else abs(
                bytes
            )
        if (absB < 1024) {
            return "$bytes B"
        }
        var value = absB
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var i = 40
        while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
            value = value shr 10
            ci.next()
            i -= 10
        }
        value *= java.lang.Long.signum(bytes).toLong()
        value /= 1024L
        return java.lang.String.format(Locale.ROOT, "%d %cB", value.toInt(), ci.current())
    }

    private var count = 0
    fun dpToPx(
        @Dimension(unit = Dimension.DP) dp: Float
    ): Float {
        val r = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            r.displayMetrics
        )
    }

    lateinit var m: MyApp

    @Inject
    lateinit var abc: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        this.fullScreenActivity()
        m = application as MyApp
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        root.quickSnack("Hey")

        abc.getMutable().observe(this, {
            //logit(it)
        })

        oof2.setOnClickListener {
            wew("yes")
            startActivity(Intent(this, MainActivity::class.java))
        }
        oof2.setOnLongClickListener {
            wew()
            startActivity(Intent(this, MainActivity::class.java))
            true
        }
        //supportFragmentManager.beginTransaction().replace(root.id, Test2() as Fragment).commit()
        //animateNotificationWithColor(Color.RED)
    }


    private fun wew(string: String = "99") {
        p++
        abc.postInData(p, string)
    }

    fun animateNotificationWithColor(color: Int) {
        val leftView: ImageView = notification_animation_left_solid
        leftView.setColorFilter(color)
        var mLightAnimator = ValueAnimator.ofFloat(*floatArrayOf(0.0f, 2.0f))
        val repeats = 0
        mLightAnimator.duration = 2000
        mLightAnimator.repeatCount = ValueAnimator.INFINITE
        mLightAnimator.repeatMode = ValueAnimator.RESTART
        mLightAnimator.addUpdateListener { animation ->
            val progress =
                (animation.animatedValue as Float).toFloat()
            leftView.scaleY = progress
            var alpha = 1.0f
            if (progress <= 0.7f) {
                alpha = progress / 0.7f
            } else if (progress >= 1.0f) {
                alpha = 2.0f - progress
            }
            leftView.alpha = alpha
            logit(progress)
        }
        mLightAnimator.start()
    }

    suspend fun time(block: suspend () -> Unit) {
        measureTimeMillis {
            block()
        }.run {
            logit("${this / 1e3}sec ${Thread.currentThread()}")
        }
    }

    var transX: Float = 0f
    var p: Double = 0.0


    fun doshit(view: View) {
        var moveX =
            ObjectAnimator.ofFloat(
                ticker_comeback,
                View.TRANSLATION_Y,
                ticker_comeback.translationY,
                0f
            )
        moveX.addUpdateListener {
            if (it.animatedValue == 0f) {
                TransitionManager.beginDelayedTransition(ticker_comeback)
                childOne.visibility = View.VISIBLE
                moveX =
                    ObjectAnimator.ofFloat(
                        ticker_comeback,
                        View.TRANSLATION_Y,
                        0f,
                        -ticker_comeback.translationY
                    )

                childOne.postDelayed({
                    TransitionManager.beginDelayedTransition(ticker_comeback)
                    childOne.visibility = View.GONE

                    moveX.setFloatValues(0f, dpToPx(-58f))
                    AnimatorSet().run {
                        interpolator = OvershootInterpolator()
                        duration = 250
                        playTogether(moveX)
                        startDelay = 1000
                        start()
                    }
                }, 2000)
            }
        }
        AnimatorSet().run {
            interpolator = OvershootInterpolator()
            duration = 250
            playTogether(moveX)
            start()
        }
    }
}

private infix fun Any.into(string: String.Companion) = this.toString()


/*

                    /*rootParent.getChildAt(0)
                        .setPadding(
                            dpToPx(6f).roundToInt(),
                            dpToPx(6f).roundToInt(),
                            dpToPx(6f).roundToInt(),
                            dpToPx(6f).roundToInt()
                        )*/    val rotateIt = test.rotation
        val transX = test.translationX
        val parent = root.getChildAt(0) as MaterialCardView

        parent.setOnClickListener { _ ->
            val isOn = count % 2 == 0
            AnimatorSet().run {
                //interpolator = AccelerateDecelerateInterpolator()
                duration = 600
                playTogether(
                    ObjectAnimator.ofFloat(
                        test,
                        View.TRANSLATION_X,
                        transX,
                        transX * 2,
                        transX
                    ),
                    ObjectAnimator.ofFloat(test, View.ROTATION, rotateIt, 90f, rotateIt)
                        .let { objectAnimator ->
                            objectAnimator.addUpdateListener { animator ->
                                logit(animator.animatedFraction.times(100).roundToInt())
                                when (animator.animatedFraction.times(100).roundToInt()) {
                                    0 -> {
                                        parent.isEnabled = false
                                    }
                                    in 40..50 -> {
                                        dhummy.text = if (isOn) "On" else "Off"
                                    }
                                    100 -> {
                                        parent.isEnabled = true
                                    }
                                    else -> {
                                    }
                                }
                            }
                            objectAnimator
                        }
                )
                start()
            }
            logit(count)
            count++
        }
    override fun onClick(p0: View) {
        /*logit(p0)
        TransitionManager.beginDelayedTransition(root)
        placeholder.setContentId(p0.id)*/
    }
/*CoroutineScope(Dispatchers.IO).launch {
            speed.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso")
        }
        CoroutineScope(Dispatchers.IO).launch {
            doshit()
        }
grouped.run {
    visibility = if (visibility == View.VISIBLE)
        View.GONE
    else
        View.VISIBLE
}

suspend fun time(block: suspend () -> Unit) {
    val startTime = System.nanoTime()

    block()

    val endTime = System.nanoTime()
    logit("${(endTime - startTime) / 1.0e9}  ${Thread.currentThread().name}")
}

fun timee(block: () -> Unit) {
    val startTime = System.nanoTime()

    block()

    val endTime = System.nanoTime()
    logit("${(endTime - startTime) / 1.0e9}  ${Thread.currentThread().name}")
}

fun getIp(string: String): String {
    return java.net.URL(string).readText()
}
executorService.execute(Runnable {
        timee {
            for (i in 1..100) {
                getIp("https://raw.githubusercontent.com/Dixzz/test_contact/master/test.json")
            }
        }
    })
    CoroutineScope(Dispatchers.IO).launch {
        time {
            for (i in 1..100) {
                getIp("https://raw.githubusercontent.com/Dixzz/test_contact/master/test.json")
            }
        }
    }
}
CoroutineScope(Dispatchers.IO).launch {
    val a = async {
        funA()
    }
    val b = async {
        funB()
    }
    val c = async {
        funC(a.await() + b.await())
    }
    t.done(c.await())
}*/

}

private fun funA(): Int {
    Thread.sleep(2000L)
    return 1
}

fun funB(): Int {
    Thread.sleep(10000L)
    return 2
}

fun funC(int: Int): Int {
    Thread.sleep(3000L)
    return 3 + int
}fun View.something() {
    val start = Color.DKGRAY
    val mid = Color.MAGENTA
    val end = Color.BLUE

//content.background is set as a GradientDrawable in layout xml file
    val gradient = background as GradientDrawable

    val evaluator = ArgbEvaluator()
    val animator = TimeAnimator.ofFloat(0.0f, 1.0f)
    animator.duration = 3000
    animator.interpolator = AccelerateDecelerateInterpolator()
    animator.repeatCount = ValueAnimator.INFINITE
    animator.repeatMode = ValueAnimator.REVERSE
    animator.addUpdateListener {
        val fraction = it.animatedFraction
        val newStart = evaluator.evaluate(fraction, start, end) as Int
        val newMid = evaluator.evaluate(fraction, mid, start) as Int
        val newEnd = evaluator.evaluate(fraction, end, mid) as Int

        gradient.useLevel = true
        gradient.orientation = GradientDrawable.Orientation.RIGHT_LEFT
        gradient.colors = intArrayOf(newStart, newMid, newEnd)
    }

    animator.start()
}
fun View.smooth() {
        //val colorFrom = background as ColorDrawable
        val colorFrom = Color.BLACK
        val colorTo = Color.WHITE
        val colorAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.run {
            duration = 500
            Log.d(TAG, "smooth ee : ${this.animatedValue}")
            addUpdateListener {
                Log.d(TAG, "smooth: ${it.animatedValue}")
                setBackgroundColor(it.animatedValue as Int)
            }
            start()
        }
    }

    fun View.closeReveal(x: Int, y: Int) {
        ViewAnimationUtils.createCircularReveal(
            this,
            x / 2,
            y / 2,
            x.coerceAtLeast(y).toFloat(),
            0f
        )
            .apply {
                duration = 500
                interpolator = OvershootInterpolator()
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {

                    }

                    override fun onAnimationEnd(p0: Animator?) {

                        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        //finish()
                        Log.d(TAG, "onAnimationEnd: Reached")
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationStart(p0: Animator?) {
                        window.decorView.smooth()
                    }
                })
                startDelay = 500
                start()
            }
    }

    fun View.circularReveal() {
        val x = this.width.toFloat()
        val y = this.height.toFloat()

        ViewAnimationUtils.createCircularReveal(
            this, (x / 2).toInt(), (y / 2).toInt(), 0f, x.coerceAtLeast(y)
        ).apply {
            duration = 1000
            interpolator = CubicAccelerateDecelerateInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    this@circularReveal.postDelayed({
                        closeReveal(x.toInt(), y.toInt())
                    }, 2000)
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }
            })
            start()
        }
    }
    private fun blurImage(context: Context, inputBitmap: Bitmap): Bitmap? {
        val BLUR_RADIUS = 25f
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs = RenderScript.create(context)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(BLUR_RADIUS)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        tmpOut.destroy()
        tmpIn.destroy()
        theIntrinsic.destroy()
        rs.finish()
        return outputBitmap
    }
 */