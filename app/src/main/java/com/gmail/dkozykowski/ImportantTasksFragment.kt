package com.gmail.dkozykowski

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gmail.dkozykowski.databinding.FragmentImportantTasksBinding

class ImportantTasksFragment : Fragment() {
    lateinit var binding: FragmentImportantTasksBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImportantTasksBinding.inflate(inflater, container, false)
        return binding.root
    }
}