package com.gmail.dkozykowski.ui.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat.startActivity
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.databinding.ViewTaskItemBinding
import com.gmail.dkozykowski.utils.getDateAndTimeFromLong
import com.gmail.dkozykowski.ui.activity.PreviewTaskActivity


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
        binding.date.text =
            getDateAndTimeFromLong(task.date)
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
            val activity = this.context as Activity
            val intent = Intent(activity, PreviewTaskActivity::class.java)
            intent.putExtra("title", task.title)
            intent.putExtra("description", task.description)
            intent.putExtra("date", task.date)
            startActivity(this.context, intent, null)
        }
        //todo binding delete setonclick
    }
}