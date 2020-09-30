package com.gmail.dkozykowski.model

import android.view.View
import android.view.View.GONE
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.button.MaterialButton

class ActionBarButtonModel(
    private val button: MaterialButton,
    private val icon: AppCompatImageView
) {
    fun setVisible(isVisible: Boolean) {
        this.button.visibility = if(isVisible) View.VISIBLE else GONE
        this.icon.visibility = if(isVisible) View.VISIBLE else GONE
    }

    fun setIcon(iconResource: Int) {
        this.icon.setImageResource(iconResource)
    }

    fun setOnClickListener(function: () -> Unit) {
        this.button.setOnClickListener { function() }
    }
}