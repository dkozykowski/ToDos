package com.gmail.dkozykowski

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmail.dkozykowski.QueryTaskType.ALL_ACTIVE
import com.gmail.dkozykowski.databinding.FragmentActiveTasksBinding

class ActiveTasksFragment : Fragment() {
    lateinit var binding: FragmentActiveTasksBinding
    private val adapter by lazy { TaskAdapter(ALL_ACTIVE) }
    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActiveTasksBinding.inflate(inflater, container, false)
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
                is TaskViewModel.LoadViewState.Success -> adapter.updateData(viewState.data)
            }
            binding.swipeRefresh.isRefreshing = viewState is TaskViewModel.LoadViewState.Loading
        })

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadTasks(ALL_ACTIVE) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.run {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
            adapter = this@ActiveTasksFragment.adapter
        }
        viewModel.loadTasks(ALL_ACTIVE)
    }
}