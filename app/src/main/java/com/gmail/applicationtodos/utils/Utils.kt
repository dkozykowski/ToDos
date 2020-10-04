package com.gmail.applicationtodos.utils

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.applicationtodos.data.DB
import com.gmail.applicationtodos.data.model.Task
import com.gmail.applicationtodos.model.UpdateTaskDataModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val FIVE_MIN_IN_MILIS = 300000L
const val NO_ID_GIVEN_CODE = -1L

fun dateToTimestamp(dateString: String): Long {
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).parse(dateString)!!.time
}

fun timestampToDateWithDayName(time: Long): String {
    val date = Date(time)
    val formatter = SimpleDateFormat("dd.MM.yyyy (E) HH:mm", Locale.UK)
    return formatter.format(date)
}

fun timestampToDate(time: Long): String {
    val date = Date(time)
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.UK)
    return formatter.format(date)
}

fun hideKeyboard(context: Context, view: View) {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getTimeLeftText(date: Long): String {
    val timestamp = (date - System.currentTimeMillis()) / 1000

    return when {
        timestamp < 0 -> "(time passed)"
        timestamp < 300 -> "(< 5 min left)"
        timestamp < 3600 -> "(${timestamp / 60} min left)"
        timestamp < 7200 -> "(1 hour left)"
        timestamp < 86400 -> "(${timestamp / 3600} hours left)"
        timestamp < 172800 -> "(1 day left)"
        timestamp < 31536000 -> "(${timestamp / 86400} days left)"
        else -> "(> 365 days left)"
    }
}

fun openPickDateDialog(context: Context, dateEditText: TextInputEditText, startDate: Long? = null) {
    var newDateText: String? = null
    val calendar = Calendar.getInstance()
    if (startDate != null) calendar.timeInMillis = startDate
    if (!dateEditText.isTextBlank()) {
        calendar.time =
            SimpleDateFormat(
                "dd.MM.yyyy HH:mm",
                Locale.getDefault()
            ).parse(dateEditText.text.toString())!!
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            dateEditText.setText("$newDateText %02d:%02d".format(hour, minute))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            newDateText = "%02d.%02d.%d".format(dayOfMonth, month + 1, year)
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun TextInputEditText.text(): String {
    return this.text.toString()
}

fun TextInputEditText.isTextBlank(): Boolean {
    return this.text.isNullOrBlank()
}

fun AppCompatAutoCompleteTextView.isTextBlank(): Boolean {
    return this.text.isNullOrBlank()
}

fun View.setAsVisible() {
    this.visibility = View.VISIBLE
}

fun View.setAsHidden() {
    this.visibility = View.GONE
}

fun Task.updateTaskWithData(updateTaskData: UpdateTaskDataModel) {
    this.title = updateTaskData.title
    this.description = updateTaskData.description
    this.date = updateTaskData.date
}

inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>): T = f() as T
    }

fun createTaskNotificationPendingEvent(task: Task, context: Context) {
    if (task.done || task.date == null || task.date!! - System.currentTimeMillis() < FIVE_MIN_IN_MILIS)
        return
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("title", task.title)
    intent.putExtra("id", task.uid)
    val pendingIntent = PendingIntent.getBroadcast(
        context.applicationContext,
        task.uid.toInt(),
        intent,
        FLAG_UPDATE_CURRENT
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.date!!, pendingIntent)
    else
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, task.date!!, pendingIntent)
}

fun removeTaskNotificationPendingEvent(task: Task, context: Context) {
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context.applicationContext,
        task.uid.toInt(),
        intent,
        FLAG_UPDATE_CURRENT
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}

fun remakeAllNotificationsPendingEvents(context: Context) {
    GlobalScope.launch {
        withContext(Dispatchers.IO) {
            val data = DB.db.taskDao().getAllActiveTasks()
            data.forEach { task ->
                removeTaskNotificationPendingEvent(task, context)
                createTaskNotificationPendingEvent(task, context)
            }
        }
    }
}

fun View.preventDoubleClick() {
    this.isEnabled = false
    this.cancelPendingInputEvents()
    Handler().postDelayed({
        this.isEnabled = true
    }, 500)
}
