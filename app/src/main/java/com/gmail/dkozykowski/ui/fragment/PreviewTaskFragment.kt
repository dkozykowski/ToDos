package com.gmail.dkozykowski.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
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

class PreviewTaskFragment : Fragment() {
    private lateinit var binding: FragmentPreviewTaskBinding
    private lateinit var taskTitle: String
    private lateinit var taskDescription: String
    private val viewModel = TaskViewModel(PREVIEW)
    private var taskId = 0
    private var taskDate: Long? = null
    private var isTaskEditModeOn = false
    private val updateTaskObserver: (TaskViewModel.UpdateViewState) -> Unit = {
        if (it is TaskViewModel.UpdateViewState.Success) {
            binding.saveButton.isClickable = true
            showToastOnTaskEditSuccessful()
        }
    }

    private fun showToastOnTaskEditSuccessful() {
        Toast.makeText(context, getString(R.string.edit_task_success), Toast.LENGTH_SHORT).show()
    }

    init {
        isTaskEditModeOn = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        setupRemoveDateButton()
    }

    private fun setViewToCurrentMode() {
        when (isTaskEditModeOn) {
            true -> setViewToEditMode()
            false -> setViewToPreviewMode()
        }
    }

    private fun setViewToEditMode() {
        with(binding) {
            titleEditText.setText(taskTitle)
            descriptionEditText.setText(taskDescription)
            if(taskDate != null) dateEditText.setText(timestampToDate(taskDate!!))

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
            dateText.text = when (taskDate) {
                null -> "Date was not set"
                else -> timestampToDate(taskDate!!)
            }
            timeLeft.text = when (taskDate) {
                null -> null
                else -> getTimeLeftText(taskDate!!)
            }

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
            binding.dateEditText.preventDoubleClick()
            hideKeyboard(context!!, binding.root)
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
            taskDate = when(binding.dateEditText.text.isNullOrBlank()) {
                true -> null
                false -> dateToTimestamp(binding.dateEditText.text.toString())
            }
            saveEditedTaskIfValid()
        }
    }

    private fun setupTimeLeftText() {
        binding.timeLeft.text = when (taskDate) {
            null -> null
            else -> getTimeLeftText(taskDate!!)
        }
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

    private fun setupRemoveDateButton() {
        binding.removeDateButton.setOnClickListener {
            binding.dateEditText.text = null
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
        val date = arguments?.getLong("date", 0)!!
        taskDate = when (date) {
            0L -> null
            else -> date
        }
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
        return (titleIsValid && descriptionIsValid)
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
}