package com.gmail.dkozykowski.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.ui.activity.MainActivity


class AlarmReceiver : BroadcastReceiver() {
    private lateinit var builder: NotificationCompat.Builder
    private var id: Int? = null
    private lateinit var title: String
    private lateinit var description: String

    override fun onReceive(context: Context?, intent: Intent?) {
        createBuilder(context!!)
        getDataFromIntent(intent!!)
        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }
    }

    private fun getDataFromIntent(intent: Intent) {
        id = intent.getIntExtra("id", 0)
        title = intent.getStringExtra("title")!!
        description = intent.getStringExtra("description")!!
    }

    private fun createBuilder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("id", id)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        builder = NotificationCompat.Builder(context)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setVibrate(LongArray(0))
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
    }
}