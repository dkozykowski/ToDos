package com.gmail.dkozykowski.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gmail.dkozykowski.QueryTaskType.PREVIEW
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.databinding.FragmentPreviewTaskBinding
import com.gmail.dkozykowski.model.UpdateTaskDataModel
import com.gmail.dkozykowski.utils.*
import com.gmail.dkozykowski.viewmodel.TaskViewModel
import kotlinx.android.synthetic.main.fragment_preview_task.*

class PreviewTaskFragment : Fragment() {
    private lateinit var binding: FragmentPreviewTaskBinding
    private lateinit var taskTitle: String
    private lateinit var taskDescription: String
    private val viewModel = TaskViewModel(PREVIEW)
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
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_preview_task, container, false)
        loadSetupFunctions()
        return binding.root
    }

    private fun loadSetupFunctions() {
        setViewToCurrentMode()
        setupDatePicking()
        setupSaveButton()
        setupTimeLeftText()
        setupCloseButton()
        setupEditButton()
        setupCancelButton()
        setupRootOnClickEvent()
    }

    private fun setViewToCurrentMode() {
        when(isTaskEditModeOn) {
            true -> setViewToEditMode()
            false -> setViewToPreviewMode()
        }
    }

    private fun setViewToEditMode() {
        with(binding) {
            titleEditText.setText(taskTitle)
            descriptionEditText.setText(taskDescription)
            dateEditText.setText(timestampToDate(taskDate))

            editModeLayout.visibility = VISIBLE
            cancelButton.visibility = VISIBLE
            cancelButtonIcon.visibility = VISIBLE

            previewModeLayout.visibility = GONE
            editButton.visibility = GONE
            editButtonIcon.visibility = GONE
        }
    }

    private fun setViewToPreviewMode() {
        with(binding) {
            titleText.text = taskTitle
            descriptionText.text = taskDescription
            dateText.text = timestampToDate(taskDate)
            timeLeft.text = getTimeLeftText(taskDate)

            editModeLayout.visibility = GONE
            cancelButton.visibility = GONE
            cancelButtonIcon.visibility = GONE

            previewModeLayout.visibility = VISIBLE
            editButton.visibility = VISIBLE
            editButtonIcon.visibility = VISIBLE
        }
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

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            taskTitle = binding.titleEditText.text()
            taskDescription = binding.descriptionEditText.text()
            taskDate = dateToTimestamp(binding.dateEditText.text())
            saveEditedTaskIfValid()
        }
    }

    private fun setupTimeLeftText() {
        binding.timeLeft.text = getTimeLeftText(taskDate)
    }

    private fun setupCloseButton() {
        binding.closePreviewButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupEditButton() {
        binding.editButton.setOnClickListener {
            isTaskEditModeOn = true
            setViewToCurrentMode()
        }
    }

    private fun setupCancelButton() {
        binding.cancelButton.setOnClickListener {
            if (wasEditTaskSheetEdited()) {
                showExitDialog()
            } else {
                isTaskEditModeOn = false
                setViewToCurrentMode()
            }
        }
    }

    private fun setupRootOnClickEvent() {
        binding.root.setOnClickListener {
            hideKeyboard(
                context!!,
                binding.root
            )
        }
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

    private fun saveEditedTaskIfValid() {
        if (validateEditTaskSheet()) {
            binding.saveButton.isClickable = false
            isTaskEditModeOn = false
            setViewToCurrentMode()
            val updateTaskData =
                UpdateTaskDataModel(taskId, taskTitle, taskDescription, taskDate)
            viewModel.updateTask(updateTaskData)
        }
    }

    private fun loadTask() {
        taskTitle = arguments?.getString("title")!!
        taskDescription = arguments?.getString("description")!!
        taskDate = arguments?.getLong("date")!!
        taskId = arguments?.getInt("id")!!
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



    private fun validateEditTaskSheet(): Boolean {
        val titleIsValid = validateTitle()
        val descriptionIsValid = validateDescription()
        val dateIsValid = validateDate()
        return (titleIsValid && descriptionIsValid && dateIsValid)
    }

    private fun validateTitle(): Boolean {
        return when {
            binding.titleEditText.isTextBlank() -> {
                binding.titleEditText.error = "Title cannot be blank!"
                false
            }
            else -> true
        }
    }

    private fun validateDescription(): Boolean {
        return when {
            binding.descriptionEditText.isTextBlank() -> {
                binding.descriptionEditText.error = "Title cannot be blank!"
                false
            }
            else -> true
        }
    }

    private fun validateDate(): Boolean {
        return when {
            binding.dateEditText.isTextBlank() -> {
                binding.dateEditText.error = "Select the date!"
                false
            }
            else -> true
        }
    }
}