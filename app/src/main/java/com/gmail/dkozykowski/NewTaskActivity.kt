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
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_task)

        viewModel.sendTaskLiveData.observeForever(sendMessageObserver)

        setupDatePicking()
        setupTimePicking()
        setupAddTaskButton()

        binding.root.setOnClickListener {
            hideKeyboard(this, binding.root)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.sendTaskLiveData.removeObserver(sendMessageObserver)
    }

    private fun validateNewTaskSheet(): Boolean {
        var isNewTaskSheetCorrect = true

        if (binding.titleEditText.text.isNullOrBlank()) {
            binding.titleEditText.error = "Title cannot be blank!"
            isNewTaskSheetCorrect = false
        }
        if (binding.descriptionEditText.text.isNullOrBlank()) {
            binding.descriptionEditText.error = "Description cannot be blank!"
            isNewTaskSheetCorrect = false
        }
        if (binding.dateEditText.text.isNullOrBlank()) {
            binding.dateEditText.error = "Select the date!"
            isNewTaskSheetCorrect = false
        }
        if (binding.timeEditText.text.isNullOrBlank()) {
            binding.timeEditText.error = "Select the time!"
            isNewTaskSheetCorrect = false
        }

        return isNewTaskSheetCorrect
    }

    private fun setupDatePicking() {
        binding.calendarButton.setOnClickListener {
            hideKeyboard(this, binding.root)
            binding.dateEditText.error = null
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

        binding.dateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) hideKeyboard(
                this,
                binding.root
            )
        }
    }

    private fun setupTimePicking() {
        binding.timeButton.setOnClickListener {
            hideKeyboard(this, binding.root)
            binding.timeEditText.error = null
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

        binding.timeEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) hideKeyboard(
                this,
                binding.root
            )
        }
    }

    private fun setupAddTaskButton() {
        binding.addTaskButton.setOnClickListener {
            try {
                if (validateNewTaskSheet()) {
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
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
}