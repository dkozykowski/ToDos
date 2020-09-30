package com.gmail.dkozykowski.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.gmail.dkozykowski.QueryTaskType.PREVIEW
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.databinding.FragmentPreviewTaskBinding
import com.gmail.dkozykowski.model.ActionBarButtonModel
import com.gmail.dkozykowski.model.UpdateTaskDataModel
import com.gmail.dkozykowski.ui.activity.MainActivity
import com.gmail.dkozykowski.utils.*
import com.gmail.dkozykowski.viewmodel.TaskViewModel

class PreviewTaskFragment : BaseFragment() {
    private lateinit var binding: FragmentPreviewTaskBinding
    private lateinit var taskTitle: String
    private lateinit var taskDescription: String
    private val viewModel = TaskViewModel(PREVIEW)
    private var taskId = 0L
    private var taskDate: Long? = null
    private var isTaskEditModeOn = false
    override var leftActionBarButtonHandler: ActionBarButtonModel? = null
    override var rightActionBarButtonHandler: ActionBarButtonModel? = null
    private val updateTaskObserver: (TaskViewModel.UpdateViewState) -> Unit = {
        if (it is TaskViewModel.UpdateViewState.Success) {
            binding.saveButton.isClickable = true
            showToastOnTaskEditSuccessful()
            createTaskNotification(it.task, context!!)
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
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_preview_task, container, false)
        loadSetupFunctions()
        return binding.root
    }

    private fun loadSetupFunctions() {
        setupActionBarButtonsHandlers()
        setupViewModel()
        setViewToCurrentMode()
        setupDatePicking()
        setupSaveButton()
        setupTimeLeftText()
        setupCloseButton()
        setupRootOnClickEvent()
        setupRemoveDateButton()
        setupLeftActionBarButton()
        setupRightActionBarButton()
    }

    private fun setupActionBarButtonsHandlers() {
        leftActionBarButtonHandler = (activity as MainActivity).leftActionBarButton
        rightActionBarButtonHandler = (activity as MainActivity).rightActionBarButton
    }

    private fun setupViewModel() {
        viewModel.updateTaskLiveData.observeForever(updateTaskObserver)
        viewModel.context = context!!
    }

    private fun setViewToCurrentMode() {
        when (isTaskEditModeOn) {
            true -> setViewToEditMode()
            false -> setViewToPreviewMode()
        }
    }

    private fun setViewToEditMode() {
        leftActionBarButtonHandler?.setVisible(false)
        rightActionBarButtonHandler?.setIcon(R.drawable.ic_round_close_24)
        setRightButtonAsCancelEditionButton()
        with(binding) {
            titleEditText.setText(taskTitle)
            descriptionEditText.setText(taskDescription)
            if (taskDate != null) dateEditText.setText(timestampToDate(taskDate!!))
            editModeLayout.visibility = VISIBLE
            previewModeLayout.visibility = GONE
        }
    }

    private fun setRightButtonAsCancelEditionButton() {
        rightActionBarButtonHandler?.setOnClickListener {
            if (wasEditTaskSheetEdited()) {
                showExitDialog()
            } else {
                isTaskEditModeOn = false
                setViewToCurrentMode()
            }
        }
    }

    private fun setViewToPreviewMode() {
        leftActionBarButtonHandler?.setVisible(true)
        rightActionBarButtonHandler?.setIcon(R.drawable.ic_baseline_arrow_back_24)
        rightActionBarButtonHandler?.setOnClickListener { onBackPressed() }
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
            previewModeLayout.visibility = VISIBLE
        }
    }

    private fun setupDatePicking() {
        binding.dateEditText.setOnClickListener {
            binding.dateEditText.preventDoubleClick()
            hideKeyboard(context!!, binding.root)
            openPickDateDialog(context!!, binding.dateEditText, taskDate)
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
            if (validateEditTaskSheet()) {
                taskTitle = binding.titleEditText.text()
                taskDescription = binding.descriptionEditText.text()
                taskDate = when (binding.dateEditText.text.isNullOrBlank()) {
                    true -> null
                    false -> dateToTimestamp(binding.dateEditText.text.toString())
                }
                saveEditedTask()
            }
        }
    }

    private fun saveEditedTask() {
        if (validateEditTaskSheet()) {
            binding.saveButton.isClickable = false
            isTaskEditModeOn = false
            setViewToCurrentMode()
            val updateTaskData =
                UpdateTaskDataModel(taskId, taskTitle, taskDescription, taskDate)
            viewModel.updateTask(updateTaskData)
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

    private fun setupLeftActionBarButton() {
        leftActionBarButtonHandler?.setVisible(true)
        leftActionBarButtonHandler?.setIcon(R.drawable.ic_round_edit_24)
        leftActionBarButtonHandler?.setOnClickListener {
            isTaskEditModeOn = true
            setViewToCurrentMode()
        }
    }

    private fun setupRightActionBarButton() {
        rightActionBarButtonHandler?.setVisible(true)
        rightActionBarButtonHandler?.setIcon(R.drawable.ic_baseline_arrow_back_24)
        rightActionBarButtonHandler?.setOnClickListener { onBackPressed() }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.updateTaskLiveData.removeObserver(updateTaskObserver)
    }

    override fun onBackPressed() {
        if (isTaskEditModeOn) {
            if (wasEditTaskSheetEdited()) {
                showExitDialog()
            } else {
                isTaskEditModeOn = false
                setViewToCurrentMode()
            }
        } else findNavController().navigateUp()
    }

    private fun loadTask() {
        taskTitle = arguments?.getString("title")!!
        taskDescription = arguments?.getString("description")!!
        val date = arguments?.getLong("date", 0)!!
        taskDate = when (date) {
            0L -> null
            else -> date
        }
        taskId = arguments?.getLong("id")!!
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