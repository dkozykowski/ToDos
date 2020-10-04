package com.gmail.applicationtodos.ui.fragment

import androidx.fragment.app.Fragment
import com.gmail.applicationtodos.model.ActionBarButtonModel

abstract class BaseFragment: Fragment() {
    abstract var leftActionBarButtonHandler: ActionBarButtonModel?
    abstract var rightActionBarButtonHandler: ActionBarButtonModel?
    abstract fun onBackPressed()
}