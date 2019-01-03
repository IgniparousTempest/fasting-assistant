package com.caughtknee.fastingassistant

import android.content.Context
import org.joda.time.LocalTime
import org.joda.time.Minutes
import kotlin.math.min

fun timeFormat(hours: Int, minutes: Int): String {
    val h = hours.toString().padStart(2, '0')
    val m = minutes.toString().padStart(2, '0')
    return "$h:$m"
}

fun duration(hourStart: Int, minuteStart: Int, hourEnd: Int, minuteEnd: Int): Int {
    val timeStart = LocalTime(hourStart, minuteStart)
    val timeEnd = LocalTime(hourEnd, minuteEnd)
    return if (timeStart.isBeforeOrEquals(timeEnd))
        Minutes.minutesBetween(timeStart, timeEnd).minutes
    else
        (24 * 60) - Minutes.minutesBetween(timeEnd, timeStart).minutes
}

fun durationString(context: Context, duration: Int): String {
    return when {
        duration < 60 -> context.getString(R.string.duration_minutes, duration.toString())
        duration % 60 == 0 -> context.getString(R.string.duration_hours, (duration / 60).toString())
        else -> context.getString(R.string.duration_hours_minutes, (duration / 60).toString(), (duration % 60).toString())
    }
}

fun timeLeftForCurrentStatus(timeCurrent: LocalTime, hourStart: Int, minuteStart: Int, hourEnd: Int, minuteEnd: Int): String {
    val timeStart = LocalTime(hourStart, minuteStart)
    val timeEnd = LocalTime(hourEnd, minuteEnd)
    val minutes = when {
        Minutes.minutesBetween(timeCurrent, timeStart).minutes < 0 && Minutes.minutesBetween(timeCurrent, timeEnd).minutes < 0 -> min(Minutes.minutesBetween(timeCurrent, timeStart).minutes + 24 * 60, Minutes.minutesBetween(timeCurrent, timeEnd).minutes + 24 * 60)
        Minutes.minutesBetween(timeCurrent, timeStart).minutes < 0 -> Minutes.minutesBetween(timeCurrent, timeEnd).minutes
        Minutes.minutesBetween(timeCurrent, timeEnd).minutes < 0 -> Minutes.minutesBetween(timeCurrent, timeStart).minutes
        else -> min(Minutes.minutesBetween(timeCurrent, timeStart).minutes, Minutes.minutesBetween(timeCurrent, timeEnd).minutes)
    }
    return (minutes / 60 + (minutes % 60 / 30)).toString()
}

fun canEat(timeCurrent: LocalTime, hourStart: Int, minuteStart: Int, hourEnd: Int, minuteEnd: Int): Boolean {
    val timeStart = LocalTime(hourStart, minuteStart)
    val timeEnd = LocalTime(hourEnd, minuteEnd)

    return if (timeStart.isBefore(timeEnd))
        timeCurrent.isBefore(timeEnd) && timeCurrent.isAfterOrEquals(timeStart)
    else
        timeCurrent.isAfterOrEquals(timeStart) || timeCurrent.isBefore(timeEnd)
}

fun LocalTime.isAfterOrEquals(time: LocalTime) = this.isAfter(time) || this.isEqual(time)
fun LocalTime.isBeforeOrEquals(time: LocalTime) = this.isBefore(time) || this.isEqual(time)