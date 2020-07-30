package com.gmail.dkozykowski

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.databinding.ViewTaskItemBinding

class ItemListView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    private val binding: ViewTaskItemBinding =
        ViewTaskItemBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun bind(task: Task, deleteCallback: () -> Unit, updateCallback: (Task) -> Unit) {
        binding.title.text = task.title
        binding.description.text = task.description
        binding.date.text = getDateFromLong(task.date)
        binding.doneCheckbox.isChecked = task.done
        binding.doneCheckbox.setOnCheckedChangeListener { _, isChecked ->
            task.done = isChecked
            updateCallback(task)
        }
        binding.importantTaskCheckbox.setChecked(task.important, animate = false)
        binding.importantTaskCheckbox.setOnCheckStateListener {
            task.important = it
            updateCallback(task)
        }
        //todo binding delete setonclick
    }
}