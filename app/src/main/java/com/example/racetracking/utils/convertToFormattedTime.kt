package com.example.racetracking.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun String.convertToFormattedTime(inputFormat: String, outputFormat: String): String {
    val inputDateFormat = SimpleDateFormat(inputFormat, Locale.getDefault())

    return try {
        val date = inputDateFormat.parse(this)
        val outputDateFormat = SimpleDateFormat(outputFormat, Locale.getDefault())
        outputDateFormat.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        // Return an empty string or handle the error as needed
        ""
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun String.convertToFormattedDate(): String {
    val dateTime = LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy-M-dd"))
}