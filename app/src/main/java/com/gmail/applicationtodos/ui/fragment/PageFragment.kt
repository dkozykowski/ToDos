package com.gmail.applicationtodos.ui.fragment

import androidx.fragment.app.Fragment

abstract class PageFragment: Fragment() {
    abstract fun reloadTasks()
}