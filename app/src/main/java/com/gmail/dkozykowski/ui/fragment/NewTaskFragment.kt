package com.gmail.dkozykowski.ui.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.databinding.FragmentNewTaskBinding
import com.gmail.dkozykowski.utils.hideKeyboard
import com.gmail.dkozykowski.utils.stringToDate
import com.gmail.dkozykowski.viewmodel.TaskViewModel
import java.util.*

class NewTaskFragment : Fragment() {
    private lateinit var binding: FragmentNewTaskBinding
    private val viewModel: TaskViewModel = TaskViewModel()
    private val sendMessageObserver: (TaskViewModel.SendViewState) -> Unit = {
        if (it is TaskViewModel.SendViewState.Success) {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_task, container, false)

        viewModel.sendTaskLiveData.observeForever(sendMessageObserver)

        setupDatePicking()
        setupTimePicking()
        setupAddTaskButton()

        binding.root.setOnClickListener {
            hideKeyboard(context!!, binding.root)
        }

        return binding.root
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
            hideKeyboard(context!!, binding.root)
            binding.dateEditText.error = null
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context!!,
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
                context!!,
                binding.root
            )
        }
    }

    private fun setupTimePicking() {
        binding.timeButton.setOnClickListener {
            hideKeyboard(context!!, binding.root)
            binding.timeEditText.error = null
            val time = Calendar.getInstance()
            TimePickerDialog(
                context!!,
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
                context!!,
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
                Toast.makeText(context!!, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onBackPressed() {
        with(binding) {
            if (titleEditText.text.isNullOrBlank() && descriptionEditText.text.isNullOrBlank() &&
                dateEditText.text.isNullOrBlank() && timeEditText.text.isNullOrBlank()
            ) findNavController().navigateUp() else showExitDialog()
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(context!!).apply {
            setMessage("Discard changes?")
            setPositiveButton("Yes") { _, _ ->
                findNavController().navigateUp()
            }
            setNegativeButton("No", null)
        }.show()
    }
}