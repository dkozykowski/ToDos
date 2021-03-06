package com.gmail.applicationtodos.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.gmail.applicationtodos.QueryTaskType
import com.gmail.applicationtodos.QueryTaskType.TODAYS
import com.gmail.applicationtodos.databinding.FragmentTodaysTasksBinding
import com.gmail.applicationtodos.ui.adapter.TaskAdapter
import com.gmail.applicationtodos.utils.setAsVisible
import com.gmail.applicationtodos.utils.viewModelFactory
import com.gmail.applicationtodos.viewmodel.TaskViewModel
import com.gmail.applicationtodos.viewmodel.TaskViewModel.LoadViewState.Success

class TodaysTasksFragment : PageFragment() {
    lateinit var updateIdlePage: (QueryTaskType) -> Unit
    lateinit var binding: FragmentTodaysTasksBinding
    private val adapter by lazy { TaskAdapter(TODAYS, ::showEmptyInfo, updateIdlePage) }
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            viewModelFactory { TaskViewModel(TODAYS) }
        ).get(TaskViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodaysTasksBinding.inflate(inflater, container, false)
        setupAdapter()
        setupViewModel()
        setupLoadTaskLiveDataObserver()
        return binding.root
    }

    private fun setupAdapter() {
        adapter.context = context!!
    }

    private fun setupViewModel() {
        viewModel.context = context!!
    }

    private fun setupLoadTaskLiveDataObserver() {
        viewModel.loadTaskLiveData.observe(viewLifecycleOwner, Observer { viewState ->
            if (viewState is Success) {
                adapter.updateData(viewState.data)
                updateEmptyInfo()
            }
        })
    }

    private fun updateEmptyInfo() {
        binding.emptyListText.visibility = if (adapter.isDataEmpty()) VISIBLE else GONE
        binding.emptyListIcon.visibility = if (adapter.isDataEmpty()) VISIBLE else GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.emptyListIcon.setAsVisible()
        binding.emptyListText.setAsVisible()
        setupRecyclerView()
        viewModel.loadTasksWithoutFilters()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.run {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context, VERTICAL, false)
            val dividerItemDecoration = DividerItemDecoration(context, VERTICAL)
            addItemDecoration(dividerItemDecoration)
            adapter = this@TodaysTasksFragment.adapter
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTasksWithoutFilters()
    }

    private fun showEmptyInfo() {
        Handler().postDelayed({
            binding.emptyListIcon.setAsVisible()
            binding.emptyListText.setAsVisible()
        }, 250)
    }

    override fun reloadTasks() {
        if (isAdded) viewModel.loadTasksWithoutFilters()
    }
}