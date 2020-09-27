package com.gmail.dkozykowski.ui.activity

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.databinding.ActivityMainBinding
import com.gmail.dkozykowski.ui.fragment.NewTaskFragment
import com.gmail.dkozykowski.ui.fragment.PreviewTaskFragment
import com.gmail.dkozykowski.ui.fragment.SearchTasksFragment
import com.gmail.dkozykowski.utils.remakeAllNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val activityStartedFromNotificationWithId by lazy {
        intent.getLongExtra("id", -1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        DB.createDatabase(this)
        createNotificationChannel()
        remakeAllNotifications(this)
        checkIfApplicationWasEverStarted()
    }

    override fun onStart() {
        super.onStart()
        previewTaskIfAppStartedFromNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channelID = getString(R.string.channel_id)
            val channel = NotificationChannel(channelID, name, importance)
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.vibrationPattern = LongArray(1) { 400 }
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkIfApplicationWasEverStarted() {
        val sharedPref = getPreferences(MODE_PRIVATE)
        val isApplicationStarted = sharedPref.getBoolean("app_started", false)
        if (!isApplicationStarted) {
            showFirstEntryDialog()
            sharedPref.edit().run {
                putBoolean("app_started", true)
                apply()
            }
        }
    }

    private fun previewTaskIfAppStartedFromNotification() {
        if (activityStartedFromNotificationWithId != -1L) getTaskFromDatabaseAndPreview(
            activityStartedFromNotificationWithId
        )
    }

    private fun getTaskFromDatabaseAndPreview(id: Long) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val task = DB.db.taskDao().getTaskById(id)
                    previewTask(task)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun previewTask(task: Task) {
        val bundle = bundleOf(
            "title" to task.title,
            "description" to task.description,
            "date" to task.date,
            "id" to task.uid
        )
        val nav = findNavController(R.id.nav_host_fragment)
        nav.navigate(R.id.previewTaskFragment, bundle)
    }

    private fun showFirstEntryDialog() {
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setMessage("Some devices may cancel task reminder notifications due to battery optimization features. In order to fix that issue, open ToDos after each reboot to reschedule reminders.")
            setPositiveButton("OK") { _, _ -> }
        }.show()
    }

    override fun onBackPressed() {
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        when (val fragment = navHostFragment!!.childFragmentManager.fragments[0]) {
            is PreviewTaskFragment -> fragment.onBackPressed()
            is NewTaskFragment -> fragment.onBackPressed()
            is SearchTasksFragment -> fragment.onBackPressed()
        }
    }
}


