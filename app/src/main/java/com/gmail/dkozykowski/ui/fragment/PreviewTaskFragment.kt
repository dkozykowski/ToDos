package com.gmail.dkozykowski.ui.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.databinding.FragmentPreviewTaskBinding
import com.gmail.dkozykowski.utils.*
import com.gmail.dkozykowski.viewmodel.TaskViewModel
import java.util.*

class PreviewTaskFragment : Fragment() {
    private lateinit var binding: FragmentPreviewTaskBinding
    private lateinit var title: String
    private lateinit var description: String
    private val viewModel = TaskViewModel()
    private var uid = 0
    private var date = 0L
    private var isEditMode = false

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

        isEditMode = false
        loadTask()
        viewModel.updateTaskLiveData.observeForever(updateTaskObserver)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_preview_task, container, false)
        updateMode()
        setupDatePicking()
        setupTimePicking()
        setupSaveButton()
        setTimeLeftText(date)

        binding.closePreviewButton.setOnClickListener {
            findNavController().navigate(R.id.action_PreviewTaskFragment_to_viewPagerFragment)
        }

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
        if (isEditMode) {
            if (areChangesUnsaved()) {
                showExitDialog()
            } else {
                isEditMode = false
                updateMode()
            }
        } else findNavController().navigate(R.id.action_PreviewTaskFragment_to_viewPagerFragment)
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

                viewModel.updateTask(uid, title, description, date)
            }
        }
    }

    private fun loadTask() {
        title = arguments?.getString("title")!!
        description = arguments?.getString("description")!!
        date = arguments?.getLong("date")!!
        uid = arguments?.getInt("id")!!
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
            titleHeader.visibility = type
            descriptionText.visibility = type
            descriptionHeader.visibility = type
            dateAndTimeText.visibility = type
            dateAndTimeHeader.visibility = type
            closePreviewButton.visibility = type
            editButton.visibility = type
            editButtonIcon.visibility = type
            line1.visibility = type
            line2.visibility = type
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
        AlertDialog.Builder(context!!).apply {
            setMessage("Discard changes?")
            setPositiveButton("Yes") { _, _ ->
                isEditMode = false
                updateMode()
            }
            setNegativeButton("No", null)
        }.show()
    }

    private fun setupDatePicking() {
        binding.dateEditText.setOnClickListener {
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
            if (hasFocus && isEditMode) {
                hideKeyboard(context!!, binding.root)
                binding.dateEditText.callOnClick()
            }
        }
    }

    private fun setupTimePicking() {
        binding.timeEditText.setOnClickListener {
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
            if (hasFocus && isEditMode) {
                hideKeyboard(context!!, binding.root)
                binding.timeEditText.callOnClick()
            }
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

    private fun setTimeLeftText(date: Long) {
        val timestamp = (date - System.currentTimeMillis()) / 1000
        binding.timeLeft.text =
            when {
                timestamp < 0 -> "(time passed)"
                timestamp < 300 -> "(< 5 min left)"
                timestamp < 3600 -> "(${timestamp / 60} min left)"
                timestamp < 172800 -> "(1 day left)"
                timestamp < 31536000 -> "(${timestamp / 86400} days left)"
                else -> "(> 365 days left)"
            }
    }
}