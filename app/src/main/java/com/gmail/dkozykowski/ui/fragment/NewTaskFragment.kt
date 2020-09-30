package com.gmail.dkozykowski.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.gmail.dkozykowski.QueryTaskType.NEW
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.databinding.FragmentNewTaskBinding
import com.gmail.dkozykowski.model.ActionBarButtonModel
import com.gmail.dkozykowski.ui.activity.MainActivity
import com.gmail.dkozykowski.utils.*
import com.gmail.dkozykowski.viewmodel.TaskViewModel
import kotlinx.android.synthetic.main.activity_main.*


class NewTaskFragment : BaseFragment() {
    private lateinit var binding: FragmentNewTaskBinding
    private val viewModel: TaskViewModel = TaskViewModel(NEW)
    override var leftActionBarButtonHandler: ActionBarButtonModel? = null
    override var rightActionBarButtonHandler: ActionBarButtonModel? = null
    private val sendMessageObserver: (TaskViewModel.SendViewState) -> Unit = {
        if (it is TaskViewModel.SendViewState.Success) {
            findNavController().navigateUp()
            createTaskNotification(it.task, context!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_task, container, false)
        loadSetupFunctions()
        return binding.root
    }

    private fun loadSetupFunctions() {
        setupActionBarButtonsHandlers()
        setupViewModel()
        setupDatePicking()
        setupAddTaskButton()
        setupOtherButtons()
        setupLeftActionBarButton()
        setupRightActionBarButton()
    }

    private fun setupActionBarButtonsHandlers() {
        leftActionBarButtonHandler = (activity as MainActivity).leftActionBarButton
        rightActionBarButtonHandler = (activity as MainActivity).rightActionBarButton
    }

    private fun setupViewModel() {
        viewModel.context = context!!
        viewModel.sendTaskLiveData.observeForever(sendMessageObserver)
    }

    private fun setupAddTaskButton() {
        binding.addTaskButton.setOnClickListener {
            try {
                sendTaskIfValid()
            } catch (e: Exception) {
                Toast.makeText(context!!, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendTaskIfValid() {
        if (validateNewTaskSheet()) {
            val task = getTaskFromSheet()
            viewModel.sendTask(task)
        }
    }

    private fun getTaskFromSheet(): Task {
        return Task(
            0,
            binding.titleEditText.text(),
            binding.descriptionEditText.text(),
            when (binding.dateEditText.text.isNullOrBlank()) {
                true -> null
                false -> dateToTimestamp(binding.dateEditText.text())
            },
            false,
            false
        )
    }

    private fun setupOtherButtons() {
        binding.root.setOnClickListener { hideKeyboard(context!!, binding.root) }
        binding.removeDateButton.setOnClickListener { binding.dateEditText.text = null }
    }

    private fun setupLeftActionBarButton() {
        leftActionBarButtonHandler?.setVisible(false)
    }

    private fun setupRightActionBarButton() {
        rightActionBarButtonHandler?.setVisible(true)
        rightActionBarButtonHandler?.setIcon(R.drawable.ic_baseline_arrow_back_24)
        rightActionBarButtonHandler?.setOnClickListener { onBackPressed() }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.sendTaskLiveData.removeObserver(sendMessageObserver)
    }

    private fun validateNewTaskSheet(): Boolean {
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

    private fun setupDatePicking() {
        binding.dateEditText.setOnClickListener {
            binding.dateEditText.preventDoubleClick()
            hideKeyboard(context!!, binding.root)
            binding.dateEditText.error = null
            openPickDateDialog(context!!, binding.dateEditText)
        }
        binding.dateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideKeyboard(context!!, binding.root)
                binding.dateEditText.callOnClick()
            }
        }
    }

    override fun onBackPressed() {
        with(binding) {
            if (titleEditText.isTextBlank() && descriptionEditText.isTextBlank() &&
                dateEditText.isTextBlank()
            ) findNavController().navigateUp()
            else showExitDialog()
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