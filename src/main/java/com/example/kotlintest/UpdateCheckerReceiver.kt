package com.example.kotlintest

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong


private const val TAG = "UpdateCheckerReceiver"

private const val DAILY_CHECK_ACTION = "daily_check_action"
private const val PROP_BUILD_DATE = "ro.build.date.utc"
private const val ONESHOT_CHECK_ACTION = "oneshot_check_action"
private const val NEW_UPDATES_NOTIFICATION_ID = 1019
private const val NEW_UPDATES_NOTIFICATION_CHANNEL = "new_updates_notification_channel"
private const val FORCE_CHECK_UPDATE =
    "RECHECK_UPDATE_OCTAVI" // can be used to check update using SystemUI or some other broadcast

class UpdateCheckerReceiver : BroadcastReceiver() {
    private lateinit var context: Context
    private val updateCheckerUrl = "https://downloads.octavi-os.com/"
    private val dateFormat = SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.ROOT)
    private lateinit var myDevice: String

    private lateinit var notificationManager: NotificationManager

    private fun test() {
        val speed = SpeedTestSocket()
        speed.addSpeedTestListener(object : ISpeedTestListener {
            override fun onCompletion(report: SpeedTestReport?) {

                report?.let { report1 ->
                    val rateInBytes = report1.transferRateBit.multiply(BigDecimal(0.125))
                    executorService.execute {
                        try {
                            Jsoup.connect(updateCheckerUrl).get()?.run {
                                select("#file-list > li:nth-child(2)").forEach { it ->

                                    // Check if device is present in official list
                                    if (it.children()
                                            .select("div > div.flex-1.truncate:containsOwn($myDevice)")
                                            .hasText()
                                    ) {
                                        try {
                                            val doc =
                                                Jsoup.connect("$updateCheckerUrl?dir=$myDevice")
                                                    .method(Connection.Method.GET)
                                                    .get()
                                            val m = doc.children()
                                                .select("#file-list > li:nth-child(2) > a:nth-child(1) > div")
                                                .text()
                                            m?.let {
                                                val str = it.split(" ")
                                                val size = str[1]

                                                val date = str[2] + " " + str[3]
                                                val dateCurr: Long = System.currentTimeMillis()
                                                val currentDate = Date(dateCurr)

                                                dateFormat.parse(date)?.let { it1 ->
                                                    if (it1 < currentDate) {
                                                        // date in website is lesser than our current checked date
                                                        // check tomorrow again

                                                        notificationManager.cancel(
                                                            NEW_UPDATES_NOTIFICATION_ID
                                                        )
                                                        scheduleRepeatingUpdatesCheck()
                                                    } else {
                                                        var eta = "X"
                                                        if (size.contains("GB")) {
                                                            eta = TimeUnit.SECONDS.toMinutes(
                                                                BigDecimal(
                                                                    size.replace(
                                                                        "GB",
                                                                        ""
                                                                    )
                                                                ).toFloat().div(
                                                                    rateInBytes.divide(
                                                                        BigDecimal(
                                                                            1e9
                                                                        )
                                                                    ).toFloat()
                                                                )
                                                                    .roundToLong()
                                                            ).toString()
                                                        } else if (size.contains("MB")) {
                                                            eta = TimeUnit.SECONDS.toMinutes(
                                                                BigDecimal(
                                                                    size.replace(
                                                                        "MB",
                                                                        ""
                                                                    )
                                                                ).toFloat().div(
                                                                    rateInBytes.divide(
                                                                        BigDecimal(
                                                                            1e6
                                                                        )
                                                                    ).toFloat()
                                                                )
                                                                    .roundToLong()
                                                            ).toString()
                                                        }
                                                        // Update was found notify the user and schedule for next update check
                                                        showNotification(str[0], eta, size)
                                                        updateRepeatingUpdatesCheck()
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            notificationManager.cancel(NEW_UPDATES_NOTIFICATION_ID)
                                            updateRepeatingUpdatesCheck()
                                        }
                                    } else {
                                        notificationManager.cancel(NEW_UPDATES_NOTIFICATION_ID)
                                        updateRepeatingUpdatesCheck()
                                    }

                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e(TAG, "onCompletion: ${e.message}")
                            notificationManager.cancel(NEW_UPDATES_NOTIFICATION_ID)
                            scheduleUpdatesCheck()
                        }
                    }
                }
            }

            override fun onProgress(percent: Float, report: SpeedTestReport?) {
            }

            override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                Log.e(TAG, "onError: $errorMessage")
                scheduleUpdatesCheck()
                notificationManager.cancel(NEW_UPDATES_NOTIFICATION_ID)
            }
        })
        speed.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso")
    }

    val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    override fun onReceive(c: Context, intent: Intent) {
        Log.d(TAG, "onReceive: $intent")
        myDevice = ""
        this.context = c

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            NEW_UPDATES_NOTIFICATION_CHANNEL,
            context.javaClass.simpleName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)


        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network not available, scheduling new check")
            scheduleUpdatesCheck()
            return
        } else {
            executorService.execute {
                checkingUpdateNotification()
                test()
            }
        }

        if (Intent.ACTION_BOOT_COMPLETED == intent.action || "hello_octavi" == intent.action) {
            if (!isNetworkAvailable()) {
                Log.d(TAG, "Network not available, scheduling new check")
                scheduleUpdatesCheck()
                return
            } else {
                executorService.execute {
                    checkingUpdateNotification()
                    test()
                }
            }
        }
    }

    private fun updateRepeatingUpdatesCheck() {
        cancelRepeatingUpdatesCheck()
        scheduleRepeatingUpdatesCheck()
    }

    private fun cancelRepeatingUpdatesCheck() {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        notificationManager.cancel(NEW_UPDATES_NOTIFICATION_ID)
        alarmMgr.cancel(getRepeatingUpdatesCheckIntent())
    }

    private fun getUpdateCheckInterval() = AlarmManager.INTERVAL_DAY
    private fun getRepeatingUpdatesCheckIntent(): PendingIntent {
        return Intent(context, UpdateCheckerReceiver::class.java).run {
            action = DAILY_CHECK_ACTION
            PendingIntent.getBroadcast(context, 0, this, 0)
        }
    }

    private fun scheduleRepeatingUpdatesCheck() {

        val updateCheckIntent: PendingIntent = getRepeatingUpdatesCheckIntent()
        val alarmMgr =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr.setRepeating(
            AlarmManager.RTC, System.currentTimeMillis() +
                    getUpdateCheckInterval(), getUpdateCheckInterval(),
            updateCheckIntent
        )
        val nextCheckDate = Date(
            System.currentTimeMillis() +
                    getUpdateCheckInterval()
        )
        Log.d(TAG, "Setting automatic updates check: $nextCheckDate")
    }

    private fun getUpdatesCheckIntent(): PendingIntent {
        return Intent(context, UpdateCheckerReceiver::class.java).run {
            action = ONESHOT_CHECK_ACTION
            PendingIntent.getBroadcast(context, 0, this, 0)
        }
    }

    private fun checkingUpdateNotification() {
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            NEW_UPDATES_NOTIFICATION_CHANNEL
        ).apply {
            setContentTitle("Checking for System Update")
            setSmallIcon(R.drawable.refresh)
            setAutoCancel(false)
            setOngoing(true)
            setProgress(0, 0, true)
        }
        notificationManager.notify(NEW_UPDATES_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun showNotification(summary: String, eta: String, size: String) {
        Log.d(TAG, "showNotification: showing notif")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("$updateCheckerUrl?dir=$myDevice"))
        val intent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            NEW_UPDATES_NOTIFICATION_CHANNEL
        ).apply {
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.refresh)
            setContentIntent(intent)
            setContentTitle("New update received")
            setStyle(
                NotificationCompat.BigTextStyle().bigText(summary)
                    .setBigContentTitle("$eta mins required to download").setSummaryText(size)
            )
            setAllowSystemGeneratedContextualActions(true)
            addAction(0, "View", intent)
            setVibrate(longArrayOf(5000, 5000))
            setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            setAutoCancel(true)
            setOngoing(false)
        }

        notificationManager.notify(NEW_UPDATES_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun scheduleUpdatesCheck() {
        val millisToNextCheck = AlarmManager.INTERVAL_HOUR * 2
        val updateCheckIntent: PendingIntent = getUpdatesCheckIntent()
        val alarmMgr =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + millisToNextCheck] =
            updateCheckIntent
        val nextCheckDate = Date(System.currentTimeMillis() + millisToNextCheck)
        notificationManager.cancel(NEW_UPDATES_NOTIFICATION_ID)
        Log.d(TAG, "Setting one-shot updates check: $nextCheckDate")
    }

    private fun isNetworkAvailable(): Boolean =
        context.getSystemService(Context.CONNECTIVITY_SERVICE).run {
            this as ConnectivityManager
            getNetworkCapabilities(activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        }
}