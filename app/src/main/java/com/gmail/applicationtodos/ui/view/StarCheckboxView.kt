package com.gmail.applicationtodos.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import com.gmail.applicationtodos.R
import kotlinx.android.synthetic.main.view_star_checkbox.view.*
import kotlin.math.sqrt


class StarCheckboxView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {
    private var active: Boolean = false
    private var changeCheckboxStateCallback: ((Boolean) -> Unit)? = null

    init {
        inflate(context, R.layout.view_star_checkbox, this)

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        setBackgroundResource(outValue.resourceId)

        importanceButtonActive.setScale(0f)
        setOnClickListener {
            setChecked(!active)
        }
    }

    fun setChecked(checked: Boolean, animate: Boolean = true) {
        if (active == checked) return
        active = checked

        if(!animate) {
            importanceButtonActive.animate().cancel()
            importanceButtonActive.setScale(if (checked) 1f else 0f)
            return
        }


        if (changeCheckboxStateCallback != null) post { changeCheckboxStateCallback!!(checked) }
        val targetScale = if (checked) 1f else 0f
        importanceButtonActive.setScale(if (checked) 0f else 1f)
        importanceButtonActive.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setInterpolator{ x -> sqrt(x) }
            .setDuration(70)
            .start()

        importanceButtonInactive.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setInterpolator { x -> -4*x*(x-1) }
            .setDuration(60)
            .start()
    }

    fun setOnCheckStateListener(callback: (Boolean) -> Unit) {
        changeCheckboxStateCallback = callback
    }

    private fun View.setScale(scale: Float) {
        this.scaleX = scale
        this.scaleY = scale
    }
}