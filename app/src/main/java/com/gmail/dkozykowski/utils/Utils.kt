package com.gmail.dkozykowski.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

fun stringToDate(dateString: String): Long {
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).parse(dateString)!!.time
}


fun getDateAndTimeFromLong(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm")
    return format.format(date)
}

fun getDateFromLong(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("dd.MM.yyyy")
    return format.format(date)
}

fun getTimeFromLong(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("HH:mm")
    return format.format(date)
}

fun hideKeyboard(context: Context, view: View) {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getTimeLeftText(date: Long) : String{
    val timestamp = (date - System.currentTimeMillis()) / 1000

    return when {
        timestamp < 0 -> "(time passed)"
        timestamp < 300 -> "(< 5 min left)"
        timestamp < 3600 -> "(${timestamp / 60} min left)"
        timestamp < 7200 -> "1 hour left"
        timestamp < 86400 -> "(${timestamp / 3600} hours left)"
        timestamp < 172800 -> "(1 day left)"
        timestamp < 31536000 -> "(${timestamp / 86400} days left)"
        else -> "(> 365 days left)"
    }
}

fun openPickDateDialog(context: Context, dateEditText: TextInputEditText) {
    val calendar = Calendar.getInstance()
    if (!dateEditText.text.isNullOrBlank()) {
        calendar.time =
            SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault()
            ).parse(dateEditText.text.toString())!!
    }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            dateEditText.setText("%02d.%02d.%d".format(dayOfMonth, month + 1, year))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun openPickTimeDialog(context: Context, timeEditText: TextInputEditText) {
    val time = Calendar.getInstance()
    if (!timeEditText.text.isNullOrBlank()) {
        time.time =
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).parse(timeEditText.text.toString())!!
    }
    TimePickerDialog(
        context,
        { _, hour, minute ->
            timeEditText.setText("%02d:%02d".format(hour, minute))
        },
        time.get(Calendar.HOUR_OF_DAY),
        time.get(Calendar.MINUTE),
        true
    ).show()
}

