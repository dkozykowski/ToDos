package com.gmail.dkozykowski.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.gmail.dkozykowski.QueryTaskType
import com.gmail.dkozykowski.QueryTaskType.TODAYS
import com.gmail.dkozykowski.databinding.FragmentTodaysTasksBinding
import com.gmail.dkozykowski.ui.adapter.TaskAdapter
import com.gmail.dkozykowski.utils.setAsHidden
import com.gmail.dkozykowski.utils.setAsVisible
import com.gmail.dkozykowski.viewmodel.TaskViewModel

class TodaysTasksFragment(private val updateIdlePage: (QueryTaskType) -> Unit) : Fragment() {
    lateinit var binding: FragmentTodaysTasksBinding
    private val adapter by lazy { TaskAdapter(TODAYS, context!!, ::showEmptyInfo, updateIdlePage) }
    lateinit var viewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodaysTasksBinding.inflate(inflater, container, false)
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

        binding.emptyListIcon.setAsHidden()
        binding.emptyListText.setAsHidden()
        binding.recyclerView.run {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context, VERTICAL, false)
            val dividerItemDecoration = DividerItemDecoration(context, VERTICAL)
            addItemDecoration(dividerItemDecoration)
            adapter = this@TodaysTasksFragment.adapter
        }
        viewModel.loadTasks(TODAYS)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTasks(TODAYS)
    }

    private fun showEmptyInfo() {
        Handler().postDelayed({
            binding.emptyListIcon.setAsVisible()
            binding.emptyListText.setAsVisible()
        }, 250)
    }
}