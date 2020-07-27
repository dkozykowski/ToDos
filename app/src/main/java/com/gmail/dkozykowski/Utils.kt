package com.gmail.dkozykowski

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import java.text.SimpleDateFormat
import java.util.*

fun stringToDate(dateString: String): Long {
    return SimpleDateFormat("dd.MM.yyyy HH", Locale.getDefault()).parse(dateString)!!.time
}

fun hideKeyboard(context: Context, view: View) {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
}