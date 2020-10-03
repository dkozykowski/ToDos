package com.gmail.dkozykowski.ui.activity

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import com.gmail.dkozykowski.model.ActionBarButtonModel
import com.gmail.dkozykowski.ui.fragment.BaseFragment
import com.gmail.dkozykowski.utils.NO_ID_GIVEN_CODE
import com.gmail.dkozykowski.utils.remakeAllNotificationsPendingEvents
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val activityStartedFromNotificationWithId by lazy {
        intent.getLongExtra("id", NO_ID_GIVEN_CODE)
    }
    lateinit var leftActionBarButton: ActionBarButtonModel
    lateinit var rightActionBarButton: ActionBarButtonModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupActionBarButtons()
        loadSetupFunctions()
    }

    private fun setupActionBarButtons() {
        leftActionBarButton = ActionBarButtonModel(
            binding.leftActionBarButton, binding.leftActionBarButtonIcon
        )
        rightActionBarButton = ActionBarButtonModel(
            binding.rightActionBarButton, binding.rightActionBarButtonIcon
        )
    }

    private fun loadSetupFunctions() {
        DB.createDatabase(this)
        setSupportActionBar(applicationToolbar)
        createNotificationChannel()
        remakeAllNotificationsPendingEvents(this)
        checkIfApplicationWasEverStarted()
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
        if (activityStartedFromNotificationWithId != NO_ID_GIVEN_CODE) getTaskFromDatabaseAndPreview(
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
                    showTaskDoesNotExistToast()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showTaskDoesNotExistToast() {
        this.runOnUiThread {
            Toast.makeText(this, "This task does not exist anymore", Toast.LENGTH_LONG).show()
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
            setMessage(getString(R.string.entry_dialog))
            setPositiveButton("OK") { _, _ -> }
        }.show()
    }

    override fun onStart() {
        super.onStart()
        setupNavigation()
    }

    private fun setupNavigation() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.actionBar.fragmentLabel.text = destination.label
        }
    }

    override fun onBackPressed() {
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val fragment = navHostFragment!!.childFragmentManager.fragments[0] as BaseFragment
        fragment.onBackPressed()
    }
}
