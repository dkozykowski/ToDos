package com.gmail.dkozykowski.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.gmail.dkozykowski.QueryTaskType.ALL_ACTIVE
import com.gmail.dkozykowski.ui.adapter.TaskAdapter
import com.gmail.dkozykowski.viewmodel.TaskViewModel
import com.gmail.dkozykowski.databinding.FragmentActiveTasksBinding

class ActiveTasksFragment(private val updateIdlePage: (Int) -> Unit) : Fragment() {
    lateinit var binding: FragmentActiveTasksBinding
    private val adapter by lazy { TaskAdapter( ALL_ACTIVE, context!!, updateIdlePage, ::showEmptyInfo)}
    lateinit var viewModel: TaskViewModel

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
                is TaskViewModel.LoadViewState.Success -> {
                    adapter.updateData(viewState.data)
                    binding.emptyListText.visibility = if (adapter.isDataEmpty()) VISIBLE else GONE
                    binding.emptyListIcon.visibility = if (adapter.isDataEmpty()) VISIBLE else GONE
                }
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.emptyListIcon.visibility = VISIBLE
        binding.emptyListText.visibility = VISIBLE
        binding.recyclerView.run {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context, VERTICAL, false)
            val dividerItemDecoration = DividerItemDecoration(context, VERTICAL)
            addItemDecoration(dividerItemDecoration)
            adapter = this@ActiveTasksFragment.adapter
        }
        viewModel.loadTasks( ALL_ACTIVE )
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTasks( ALL_ACTIVE )
    }

    private fun showEmptyInfo() {
        Handler().postDelayed({
            binding.emptyListIcon.visibility = VISIBLE
            binding.emptyListText.visibility = VISIBLE
        }, 250)
    }
}