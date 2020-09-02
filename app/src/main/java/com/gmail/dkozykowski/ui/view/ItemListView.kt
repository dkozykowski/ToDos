package com.gmail.dkozykowski.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.R.id.searchTasksFragment
import com.gmail.dkozykowski.R.id.viewPagerFragment
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.databinding.ViewTaskItemBinding
import com.gmail.dkozykowski.ui.fragment.NewTaskFragment
import com.gmail.dkozykowski.ui.fragment.PreviewTaskFragment
import com.gmail.dkozykowski.ui.fragment.SearchTasksFragment
import com.gmail.dkozykowski.ui.fragment.ViewPagerFragment
import com.gmail.dkozykowski.utils.getDateAndTimeFromLong
import com.gmail.dkozykowski.utils.getTimeLeftText


class ItemListView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    private val binding: ViewTaskItemBinding =
        ViewTaskItemBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun bind(
        task: Task,
        deleteCallback: () -> Unit,
        updateCallback: (task: Task) -> Unit
    ) {
        binding.title.text = task.title
        binding.description.text = task.description
        binding.date.text = getDateAndTimeFromLong(task.date)
        binding.doneCheckbox.isChecked = task.done

        binding.doneCheckbox.setOnCheckedChangeListener { _, isChecked ->
            task.done = isChecked
            updateCallback(task)
        }

        binding.importantTaskCheckboxLayout.setOnClickListener { binding.importantTaskCheckbox.callOnClick() }

        binding.importantTaskCheckbox.setChecked(task.important, animate = false)

        binding.importantTaskCheckbox.setOnCheckStateListener {
            task.important = it
            updateCallback(task)
        }

        binding.importantTaskCheckboxLayout.setOnClickListener {
            binding.importantTaskCheckbox.isPressed = true
            binding.importantTaskCheckbox.callOnClick()
        }

        binding.root.setOnClickListener {
            val bundle = bundleOf(
                "title" to task.title,
                "description" to task.description,
                "date" to task.date,
                "id" to task.uid
            )
            if (findNavController().currentDestination?.id == viewPagerFragment) {
                findNavController().navigate(R.id.action_viewPagerFragment_to_PreviewTaskFragment, bundle)
            } else if (findNavController().currentDestination?.id == searchTasksFragment) {
                findNavController().navigate(R.id.action_searchTasksFragment_to_previewTaskFragment, bundle)
            }
        }
        binding.root.setOnLongClickListener {
            AlertDialog.Builder(context!!).apply {
                setMessage("Delete task?")
                setPositiveButton("Yes") { _, _ -> deleteCallback() }
                setNegativeButton("No", null)
            }.show()
            false
        }

        setTimeLeftText(task.date)
    }

    private fun setTimeLeftText(date: Long) {
        binding.timeLeft.text = getTimeLeftText(date)
    }
}