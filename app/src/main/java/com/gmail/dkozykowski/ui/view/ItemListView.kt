package com.gmail.dkozykowski.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.databinding.ViewTaskItemBinding
import com.gmail.dkozykowski.utils.getDateAndTimeFromLong


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
            findNavController().navigate(
                R.id.action_viewPagerFragment_to_PreviewTaskFragment,
                bundle
            )
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