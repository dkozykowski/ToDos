package com.gmail.dkozykowski.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.databinding.FragmentPreviewTaskBinding
import com.gmail.dkozykowski.utils.*
import com.gmail.dkozykowski.viewmodel.TaskViewModel

class PreviewTaskFragment : Fragment() {
    private lateinit var binding: FragmentPreviewTaskBinding
    private lateinit var taskTitle: String
    private lateinit var taskDescription: String
    private val viewModel = TaskViewModel()
    private var taskId = 0
    private var taskDate = 0L
    private var isTaskEditModeOn = false

    private val updateTaskObserver: (TaskViewModel.UpdateViewState) -> Unit = {
        if (it is TaskViewModel.UpdateViewState.Success) {
            binding.saveButton.isClickable = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        isTaskEditModeOn = false
        loadTask()
        viewModel.updateTaskLiveData.observeForever(updateTaskObserver)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_preview_task, container, false)
        setViewToCurrentMode()
        setupDatePicking()
        setupSaveButton()

        binding.timeLeft.text = getTimeLeftText(taskDate)

        binding.closePreviewButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.editButton.setOnClickListener {
            isTaskEditModeOn = true
            setViewToCurrentMode()
        }

        binding.cancelButton.setOnClickListener {
            if (wasEditTaskSheetEdited()) {
                showExitDialog()
            } else {
                isTaskEditModeOn = false
                setViewToCurrentMode()
            }
        }

        binding.root.setOnClickListener {
            hideKeyboard(
                context!!,
                binding.root
            )
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.updateTaskLiveData.removeObserver(updateTaskObserver)
    }

    fun onBackPressed() {
        if (isTaskEditModeOn) {
            if (wasEditTaskSheetEdited()) {
                showExitDialog()
            } else {
                isTaskEditModeOn = false
                setViewToCurrentMode()
            }
        } else findNavController().navigateUp()
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            taskTitle = binding.titleEditText.text()
            taskDescription = binding.descriptionEditText.text()
            taskDate = dateToTimestamp(binding.dateEditText.text())

            if (validateEditTaskSheet()) {
                binding.saveButton.isClickable = false
                isTaskEditModeOn = false
                setViewToCurrentMode()
                viewModel.updateTask(taskId, taskTitle, taskDescription, taskDate)
            }
        }
    }

    private fun loadTask() {
        taskTitle = arguments?.getString("title")!!
        taskDescription = arguments?.getString("description")!!
        taskDate = arguments?.getLong("date")!!
        taskId = arguments?.getInt("id")!!
    }

    private fun setViewToCurrentMode() {
        with(binding) {
            if (isTaskEditModeOn) {
                titleEditText.setText(taskTitle)
                descriptionEditText.setText(taskDescription)
                dateEditText.setText(timestampToDate(taskDate))
            } else {
                titleText.text = taskTitle
                descriptionText.text = taskDescription
                dateText.text = timestampToDate(taskDate)
                timeLeft.text = getTimeLeftText(taskDate)
            }

            editModeLayout.visibility = if (isTaskEditModeOn) VISIBLE else GONE
            cancelButton.visibility = if (isTaskEditModeOn) VISIBLE else GONE
            cancelButtonIcon.visibility = if (isTaskEditModeOn) VISIBLE else GONE

            previewModeLayout.visibility = if (isTaskEditModeOn) GONE else VISIBLE
            editButton.visibility = if (isTaskEditModeOn) GONE else VISIBLE
            editButtonIcon.visibility = if (isTaskEditModeOn) GONE else VISIBLE
        }
    }

    private fun wasEditTaskSheetEdited(): Boolean {
        if (binding.titleEditText.text() != taskTitle ||
            binding.descriptionEditText.text() != taskDescription ||
            dateToTimestamp(binding.dateEditText.text()) != taskDate
        ) return true
        return false
    }

    private fun showExitDialog() {
        AlertDialog.Builder(context!!).apply {
            setMessage("Discard changes?")
            setPositiveButton("Yes") { _, _ ->
                isTaskEditModeOn = false
                setViewToCurrentMode()
            }
            setNegativeButton("No", null)
        }.show()
    }

    private fun setupDatePicking() {
        binding.dateEditText.setOnClickListener {
            hideKeyboard(context!!, binding.root)
            binding.dateEditText.error = null
            openPickDateDialog(context!!, binding.dateEditText)
        }
        binding.dateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && isTaskEditModeOn) {
                hideKeyboard(context!!, binding.root)
                binding.dateEditText.callOnClick()
            }
        }
    }

    private fun validateEditTaskSheet(): Boolean {
        var isPreviewTaskSheetCorrect = true

        if (binding.titleEditText.isTextBlank()) {
            binding.titleEditText.error = "Title cannot be blank!"
            isPreviewTaskSheetCorrect = false
        }
        if (binding.descriptionEditText.isTextBlank()) {
            binding.descriptionEditText.error = "Description cannot be blank!"
            isPreviewTaskSheetCorrect = false
        }
        if (binding.dateEditText.isTextBlank()) {
            binding.dateEditText.error = "Select the date!"
            isPreviewTaskSheetCorrect = false
        }
        return isPreviewTaskSheetCorrect
    }
}