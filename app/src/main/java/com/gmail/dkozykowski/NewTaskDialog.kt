package com.gmail.dkozykowski

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.databinding.DialogNewTaskBinding
import java.util.*

class NewTaskDialog(context: Context) : AlertDialog(context) {
    private val binding = DialogNewTaskBinding.inflate(LayoutInflater.from(context))
    private val viewModel: TaskViewModel = TaskViewModel()
    private val sendMessageObserver: (TaskViewModel.SendViewState) -> Unit = {
        if (it is TaskViewModel.SendViewState.Success) {
            //updateCallback()
            dismiss()
        }
    }


    override fun show() {
        setView(binding.root)
        super.show()

        viewModel.sendTaskLiveData.observeForever(sendMessageObserver)

        setOnDismissListener {
            viewModel.sendTaskLiveData.removeObserver(sendMessageObserver)
        }

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
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        binding.root.setOnClickListener {
            hideKeyboard(context, binding.root)
            clearFocus()
        }

        binding.calendarButton.setOnClickListener {
            hideKeyboard(context, binding.root)
            clearFocus()
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    binding.dateEditText.setText("%02d.%02d.%d".format(dayOfMonth, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.timeButton.setOnClickListener {
            hideKeyboard(context, binding.root)
            clearFocus()
            val time = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    binding.timeEditText.setText("%02d:%02d".format(hour, minute))
                },
                time.get(Calendar.HOUR),
                time.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun clearFocus() {
        binding.dateEditText.clearFocus()
        binding.timeEditText.clearFocus()
        binding.descriptionEditText.clearFocus()
        binding.titleEditText.clearFocus()
    }
}