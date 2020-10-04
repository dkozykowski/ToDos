package com.gmail.applicationtodos.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.gmail.applicationtodos.R
import com.gmail.applicationtodos.R.id.searchTasksFragment
import com.gmail.applicationtodos.R.id.viewPagerFragment
import com.gmail.applicationtodos.data.model.Task
import com.gmail.applicationtodos.databinding.ViewTaskItemBinding
import com.gmail.applicationtodos.utils.getTimeLeftText
import com.gmail.applicationtodos.utils.preventDoubleClick
import com.gmail.applicationtodos.utils.timestampToDateWithDayName


class ItemListView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    private val binding: ViewTaskItemBinding =
        ViewTaskItemBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun bind(
        task: Task,
        deleteCallback: (task: Task) -> Unit,
        updateCallback: (task: Task) -> Unit
    ) {
        binding.title.text = task.title
        binding.description.text = task.description
        binding.doneCheckbox.isChecked = task.done
        setDateText(task.date)
        binding.doneCheckbox.setOnClickListener {
            task.done = !task.done
            updateCallback(task)
        }

        binding.importantTaskCheckboxLayout.setOnClickListener { binding.importantTaskCheckbox.callOnClick() }

        binding.importantTaskCheckbox.setChecked(task.important, animate = false)

        binding.importantTaskCheckbox.setOnCheckStateListener { isChecked ->
            task.important = isChecked
            updateCallback(task)
        }

        binding.importantTaskCheckboxLayout.setOnClickListener {
            binding.importantTaskCheckbox.isPressed = true
            binding.importantTaskCheckbox.callOnClick()
        }

        binding.root.setOnClickListener {
            binding.root.preventDoubleClick()
            val bundle = bundleOf(
                "title" to task.title,
                "description" to task.description,
                "date" to task.date,
                "id" to task.uid
            )
            val nav = findNavController()
            if (nav.currentDestination?.id == viewPagerFragment) {
                nav.navigate(R.id.action_viewPagerFragment_to_PreviewTaskFragment, bundle)
            } else if (nav.currentDestination?.id == searchTasksFragment) {
                nav.navigate(R.id.action_searchTasksFragment_to_previewTaskFragment, bundle)
            }
        }

        binding.root.setOnLongClickListener {
            binding.root.preventDoubleClick()
            AlertDialog.Builder(context!!).apply {
                setMessage("Delete task?")
                setPositiveButton("Yes") { _, _ -> deleteCallback(task) }
                setNegativeButton("No", null)
            }.show()
            false
        }
    }

    private fun setDateText(date: Long?) {
        binding.date.text = when (date) {
            null -> "Date was not set"
            else -> "${timestampToDateWithDayName(date)} \n${getTimeLeftText(date)}"
        }
    }
}