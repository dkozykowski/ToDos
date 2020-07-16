package com.gmail.dkozykowski

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gmail.dkozykowski.databinding.FragmentDoneTasksBinding

class DoneTasksFragment : Fragment() {
    lateinit var binding: FragmentDoneTasksBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDoneTasksBinding.inflate(inflater, container, false)
        return binding.root
    }
}