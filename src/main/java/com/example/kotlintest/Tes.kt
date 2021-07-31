package com.example.kotlintest

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun foo(vararg strings: Any) {
    for (s in strings) {
        when (s) {
            is Pair<*, *> -> {
                println("${s.first} ${s.second}")
            }
            is Triple<*, *, *> -> {
                println("${s.first} ${s.second} ${s.third}")
            }
            else -> println(s)
        }
    }
}

fun main() {
    //foo("t" to 1, "2" to "1", "3" to "2", Triple(1, 1, 1), 1e9)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE)
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val currentDate = abc().run {
        if (this > 0)
            TimeUnit.SECONDS.toMillis(this)
        else
            return
    }
    if (currentDate == 0L || currentDate == 1L) return
    try {
        dateFormat.parse("2021-07-18 10:55:33")?.let {
            println(it)
            println(Date(currentDate))
            println(it.time < currentDate) // current build of device is greater than website's
        }
    } catch (e: Exception) {
    }
    var m: String = abcc().run {
         if (this.isNotEmpty()) this
        else {

            return
        }
    }
    println(m)
}
private const val TAG = "Tes"

fun abcc() = ""

fun abc() = 10L