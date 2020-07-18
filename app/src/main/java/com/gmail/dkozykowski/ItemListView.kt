package com.gmail.dkozykowski

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gmail.dkozykowski.databinding.ViewTaskItemBinding

class ItemListView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    private val binding: ViewTaskItemBinding =
        ViewTaskItemBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun bind(task: Task) {
        binding.title.text = task.title
        binding.description.text = task.description
        //todo(binding.date.text), (binding.time.text)
        binding.doneCheckbox.isChecked = task.done
        //todo binding.root setonclick
        //todo binding delete setonclick
        //todo binding star setonclick
        //todo binding checkbox setonclick
    }
}