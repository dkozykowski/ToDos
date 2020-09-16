package com.gmail.dkozykowski.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.ui.activity.MainActivity


class AlarmReceiver : BroadcastReceiver() {
    private lateinit var builder: NotificationCompat.Builder
    private var id: Int? = null
    private lateinit var title: String
    private lateinit var description: String

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == "android.intent.action.BOOT_COMPLETED") {
            remakeAllNotifications()
        } else {
            getDataFromIntent(intent)
            createBuilder(context!!)
            with(NotificationManagerCompat.from(context)) {
                notify(0, builder.build())
            }
        }
    }

    private fun remakeAllNotifications() {
        
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
        builder = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setShowWhen(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setVibrate(LongArray(1) {400})
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
    }
}