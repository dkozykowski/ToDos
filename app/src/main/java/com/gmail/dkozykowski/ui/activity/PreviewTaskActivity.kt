package com.gmail.dkozykowski.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.databinding.ActivityPreviewTaskBinding
import com.gmail.dkozykowski.utils.*
import com.gmail.dkozykowski.viewmodel.TaskViewModel
import java.util.*

class PreviewTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewTaskBinding
    private lateinit var title: String
    private lateinit var description: String
    private val viewModel: TaskViewModel = TaskViewModel()
    private var id = 0
    private var date = 0L
    private var isEditMode = false

    private val updateTaskObserver: (TaskViewModel.UpdateViewState) -> Unit = {
        if (it is TaskViewModel.UpdateViewState.Success) {
            binding.saveButton.isClickable = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadTask()

        viewModel.updateTaskLiveData.observeForever(updateTaskObserver)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_preview_task
        )

        updateMode()
        setupDatePicking()
        setupTimePicking()
        setupSaveButton()

        binding.closePreviewButton.setOnClickListener { finish() }
        binding.editButton.setOnClickListener {
            isEditMode = true
            updateMode()
        }
        binding.cancelButton.setOnClickListener {
            if (areChangesUnsaved()) {
                showExitDialog()
            } else {
                isEditMode = false
                updateMode()
            }
        }
        binding.root.setOnClickListener {
            hideKeyboard(
                this,
                binding.root
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.updateTaskLiveData.removeObserver(updateTaskObserver)
    }

    override fun onBackPressed() {
        if (isEditMode) {
            if (areChangesUnsaved()) {
                showExitDialog()
            } else {
                isEditMode = false
                updateMode()
            }
        } else finish()
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            title = binding.titleEditText.text.toString()
            description = binding.descriptionEditText.text.toString()
            date = stringToDate("${binding.dateEditText.text} ${binding.timeEditText.text}")

            if (validateEditTaskSheet()) {
                binding.saveButton.isClickable = false
                isEditMode = false
                updateMode()

                viewModel.updateTask(id, title, description, date)
            }
        }
    }

    private fun loadTask() {
        title = intent.getStringExtra("title")!!
        description = intent.getStringExtra("description")!!
        date = intent.getLongExtra("date", 0)
        id = intent.getIntExtra("id", 0)
    }

    private fun updateMode() {

        updateEditViewProperties(if (isEditMode) View.VISIBLE else View.GONE)
        updatePreviewViewProperties(if (isEditMode) View.GONE else View.VISIBLE)

        with(binding) {
            if (isEditMode) {
                titleEditText.setText(title)
                descriptionEditText.setText(description)
                dateEditText.setText(getDateFromLong(date))
                timeEditText.setText(getTimeFromLong(date))
            } else {
                titleText.text = title
                descriptionText.text = description
                dateAndTimeText.text = getDateAndTimeFromLong(date)
            }
        }
    }

    private fun updateEditViewProperties(type: Int) {
        with(binding) {
            timeButtonIcon.visibility = type
            calendarButtonIcon.visibility = type
            timeButton.visibility = type
            calendarButton.visibility = type
            titleInputLayout.visibility = type
            descriptionInputLayout.visibility = type
            dateInputLayout.visibility = type
            timeInputLayout.visibility = type
            titleEditText.visibility = type
            descriptionEditText.visibility = type
            dateEditText.visibility = type
            timeEditText.visibility = type
            saveButton.visibility = type
            cancelButton.visibility = type
            cancelButtonIcon.visibility = type
        }
    }

    private fun updatePreviewViewProperties(type: Int) {
        with(binding) {
            titleText.visibility = type
            descriptionText.visibility = type
            dateAndTimeText.visibility = type
            closePreviewButton.visibility = type
            editButton.visibility = type
            editButtonIcon.visibility = type
        }
    }

    private fun areChangesUnsaved(): Boolean {
        if (binding.titleEditText.text.toString() != title ||
            binding.descriptionEditText.text.toString() != description ||
            stringToDate("${binding.dateEditText.text} ${binding.timeEditText.text}") != date
        ) return true
        return false
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("Discard changes?")
            setPositiveButton("Yes") { _, _ ->
                isEditMode = false
                updateMode()
            }
            setNegativeButton("No", null)
        }.show()
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

    private fun validateEditTaskSheet(): Boolean {
        var isPreviewTaskSheetCorrect = true

        if (binding.titleEditText.text.isNullOrBlank()) {
            binding.titleEditText.error = "Title cannot be blank!"
            isPreviewTaskSheetCorrect = false
        }
        if (binding.descriptionEditText.text.isNullOrBlank()) {
            binding.descriptionEditText.error = "Description cannot be blank!"
            isPreviewTaskSheetCorrect = false
        }
        if (binding.dateEditText.text.isNullOrBlank()) {
            binding.dateEditText.error = "Select the date!"
            isPreviewTaskSheetCorrect = false
        }
        if (binding.timeEditText.text.isNullOrBlank()) {
            binding.timeEditText.error = "Select the time!"
            isPreviewTaskSheetCorrect = false
        }

        return isPreviewTaskSheetCorrect
    }
}