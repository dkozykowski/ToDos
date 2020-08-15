package com.gmail.dkozykowski.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
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
        updateCallback: (task: Task, context: Context) -> Unit
    ) {
        binding.title.text = task.title
        binding.description.text = task.description
        binding.date.text = getDateAndTimeFromLong(task.date)
        binding.doneCheckbox.isChecked = task.done

        binding.doneCheckbox.setOnCheckedChangeListener { _, isChecked ->
            task.done = isChecked
            updateCallback(task, context)
        }

        binding.importantTaskCheckbox.setChecked(task.important, animate = false)

        binding.importantTaskCheckbox.setOnCheckStateListener {
            task.important = it
            updateCallback(task, context)
        }

        binding.root.setOnClickListener {
            val bundle = bundleOf(
                "title" to task.title,
                "description" to task.description,
                "date" to task.date,
                "id" to task.uid
            )
            findNavController().navigate(R.id.PreviewTaskFragment, bundle)
        }
        //todo binding delete setonclick
    }
}