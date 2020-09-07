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
import com.gmail.dkozykowski.QueryTaskType.DONE
import com.gmail.dkozykowski.databinding.FragmentDoneTasksBinding
import com.gmail.dkozykowski.ui.adapter.TaskAdapter
import com.gmail.dkozykowski.utils.setAsVisible
import com.gmail.dkozykowski.utils.viewModelFactory
import com.gmail.dkozykowski.viewmodel.TaskViewModel

class DoneTasksFragment(private val updateIdlePage: (QueryTaskType) -> Unit) : Fragment() {
    lateinit var binding: FragmentDoneTasksBinding
    private val adapter by lazy { TaskAdapter( DONE, context!!, ::showEmptyInfo, updateIdlePage)}
    lateinit var viewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDoneTasksBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
            this,
            viewModelFactory { TaskViewModel(DONE) }
        ).get(TaskViewModel::class.java)
        setupLoadTaskLiveDataObserver()
        return binding.root
    }

    private fun setupLoadTaskLiveDataObserver() {
        viewModel.loadTaskLiveData.observe(viewLifecycleOwner, Observer { viewState ->
            when (viewState) {
                is TaskViewModel.LoadViewState.Error -> showErrorMessageToast(viewState.errorMessage)
                is TaskViewModel.LoadViewState.Success -> {
                    adapter.updateData(viewState.data)
                    updateEmptyInfo()
                }
            }
        })
    }

    private fun showErrorMessageToast(errorMessage: String) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
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
            adapter = this@DoneTasksFragment.adapter
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
}