package com.gmail.dkozykowski

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.databinding.ActivityNewTaskBinding
import java.util.*

class NewTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewTaskBinding
    private val viewModel: TaskViewModel = TaskViewModel()
    private val sendMessageObserver: (TaskViewModel.SendViewState) -> Unit = {
        if (it is TaskViewModel.SendViewState.Success) {
            //updateCallback()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_task)

        viewModel.sendTaskLiveData.observeForever(sendMessageObserver)

        binding.addTaskButton.setOnClickListener {
            try {
                viewModel.sendTask(
                    Task(
                        0,
                        binding.titleEditText.text.toString(),
                        binding.descriptionEditText.text.toString(),
                        stringToDate("${binding.dateEditText.text} ${binding.timeEditText.text}"),
                        important = false,
                        done = false
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        binding.root.setOnClickListener {
            hideKeyboard(this, binding.root)
            clearFocus()
        }

        binding.calendarButton.setOnClickListener {
            hideKeyboard(this, binding.root)
            clearFocus()
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    binding.dateEditText.setText("%02d.%02d.%d".format(dayOfMonth, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.timeButton.setOnClickListener {
            hideKeyboard(this, binding.root)
            clearFocus()
            val time = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    binding.timeEditText.setText("%02d:%02d".format(hour, minute))
                },
                time.get(Calendar.HOUR),
                time.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.sendTaskLiveData.removeObserver(sendMessageObserver)
    }

    private fun clearFocus() {
        binding.dateEditText.clearFocus()
        binding.timeEditText.clearFocus()
        binding.descriptionEditText.clearFocus()
        binding.titleEditText.clearFocus()
    }
}

