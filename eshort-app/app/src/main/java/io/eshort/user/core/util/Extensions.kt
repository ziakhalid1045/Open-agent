package io.eshort.user.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Long.formatCount(): String = when {
    this >= 1_000_000_000 -> "${this / 1_000_000_000}B"
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 10_000 -> "${this / 1_000}K"
    this >= 1_000 -> "%.1fK".format(this / 1000.0)
    else -> this.toString()
}

fun Int.formatCount(): String = this.toLong().formatCount()

fun Long.timeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        days < 30 -> "${days / 7}w"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(this))
    }
}
