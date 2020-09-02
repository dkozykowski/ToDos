package com.gmail.dkozykowski.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.gmail.dkozykowski.QueryTaskType
import com.gmail.dkozykowski.QueryTaskType.SEARCH
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.databinding.FragmentSearchTasksBinding
import com.gmail.dkozykowski.ui.adapter.TaskAdapter
import com.gmail.dkozykowski.utils.hideKeyboard
import com.gmail.dkozykowski.viewmodel.TaskViewModel


class SearchTasksFragment : Fragment() {
    private lateinit var binding: FragmentSearchTasksBinding
    private val adapter by lazy { TaskAdapter(SEARCH, context!!, ::emptyFunction, ::showEmptyInfo) }
    lateinit var viewModel: TaskViewModel
    private var filtersShown = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            FragmentSearchTasksBinding.inflate(inflater, container, false)
        binding.cancelButton.setOnClickListener { onBackPressed() }

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(TaskViewModel::class.java)

        viewModel.loadTaskLiveData.observe(viewLifecycleOwner, Observer { viewState ->
            when (viewState) {
                is TaskViewModel.LoadViewState.Error -> Toast.makeText(
                    context,
                    viewState.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
                is TaskViewModel.LoadViewState.Success -> {
                    adapter.updateData(viewState.data)
                    // TODO: 01.09.2020  show empty info 
                }
            }
        })

        val items = arrayOf("none", "true", "false")

        binding.doneStatusSpinner.setAdapter(
            ArrayAdapter<String>(
                context!!,
                R.layout.dropdown_menu_popup_item,
                items
            )
        )
        binding.doneStatusSpinner.setText("none", false)

        binding.importanceStatusSpinner.setAdapter(
            ArrayAdapter<String>(
                context!!,
                R.layout.dropdown_menu_popup_item,
                items
            )
        )
        binding.importanceStatusSpinner.setText("none", false)

        binding.hideFiltersButton.setOnClickListener {
            filtersShown = !filtersShown
            binding.filtersLayout.visibility = if (filtersShown) VISIBLE else GONE
            binding.hideFiltersButton.animate().rotation(if (filtersShown) 180F else 0F)
                .setDuration(if (filtersShown) 350 else 600).start()
        }

        binding.importanceStatusSpinner.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) hideKeyboard(context!!, binding.root)
        }

        binding.doneStatusSpinner.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) hideKeyboard(context!!, binding.root)
        }

        binding.root.setOnClickListener { hideKeyboard(context!!, binding.root) }

        binding.searchButton.setOnClickListener {
            // TODO: Toast x results found
            viewModel.loadTasks(SEARCH)
        }

        return binding.root
    }


    fun onBackPressed() {
        findNavController().navigateUp()
    }

    private fun emptyFunction(variable : Int) {}

    private fun showEmptyInfo() {
        // TODO: 01.09.2020  
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: 01.09.2020 info
        binding.recyclerView.run {
            setHasFixedSize( false )
            layoutManager = LinearLayoutManager(context, VERTICAL, false)
            val dividerItemDecoration = DividerItemDecoration(context, VERTICAL)
            addItemDecoration(dividerItemDecoration)
            adapter = this@SearchTasksFragment.adapter
        }
        viewModel.loadTasks(SEARCH)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTasks(SEARCH)
    }
    
}