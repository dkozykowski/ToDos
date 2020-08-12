package com.gmail.dkozykowski.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
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

fun minimum(A: Int, B: Int): Int {
    if (A <= B) return A
    return B
}