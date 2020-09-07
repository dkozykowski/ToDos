package com.gmail.dkozykowski.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.gmail.dkozykowski.QueryTaskType
import com.gmail.dkozykowski.QueryTaskType.SEARCH
import com.gmail.dkozykowski.databinding.FragmentSearchTasksBinding
import com.gmail.dkozykowski.model.FilterTaskDataModel
import com.gmail.dkozykowski.ui.adapter.TaskAdapter
import com.gmail.dkozykowski.utils.*
import com.gmail.dkozykowski.viewmodel.TaskViewModel

class SearchTasksFragment : Fragment() {
    private lateinit var binding: FragmentSearchTasksBinding
    private val adapter by lazy { TaskAdapter(SEARCH, context!!, ::showEmptyInfo) }
    lateinit var viewModel: TaskViewModel
    private var areFiltersShown = false
    private lateinit var toast : Toast

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(
            this,
            viewModelFactory { TaskViewModel(SEARCH) }
        ).get(TaskViewModel::class.java)
        toast = Toast.makeText(context!!, "", Toast.LENGTH_SHORT)
        setupLoadTaskLiveDataObserver()
        binding = FragmentSearchTasksBinding.inflate(inflater, container, false)
        binding.cancelButton.setOnClickListener { onBackPressed() }
        binding.root.setOnClickListener { hideKeyboard(context!!, binding.root) }
        binding.searchButton.setOnClickListener {
            loadFilteredTask()
        }

        binding.hideFiltersButton.setOnClickListener {
            areFiltersShown = !areFiltersShown
            binding.filtersLayout.visibility = if (areFiltersShown) VISIBLE else GONE
            binding.hideFiltersButton.animate().rotation(if (areFiltersShown) 180F else 0F)
                .setDuration(if (areFiltersShown) 350 else 600).start()
        }

        setupDatePicking()

        return binding.root
    }

    private fun setupLoadTaskLiveDataObserver() {
        viewModel.loadTaskLiveData.observe(viewLifecycleOwner, Observer { viewState ->
            when (viewState) {
                is TaskViewModel.LoadViewState.Error -> showErrorMessageToast(viewState.errorMessage)
                is TaskViewModel.LoadViewState.Success -> {
                    adapter.updateData(viewState.data)
                    updateEmptyInfo()
                    showFoundTasksNumberToast()
                }
            }
        })
    }

    private fun showErrorMessageToast(errorMessage: String) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }


    fun onBackPressed() {
        findNavController().navigateUp()
    }

    private fun showEmptyInfo() {
        binding.emptyListText.setAsVisible()
    }

    private fun updateEmptyInfo() {
        when(adapter.isDataEmpty()) {
            true -> binding.emptyListText.setAsVisible()
            else -> binding.emptyListText.setAsHidden()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        loadFilteredTask()
    }

    private fun setupRecycleView() {
        binding.recyclerView.run {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context, VERTICAL, false)
            val dividerItemDecoration = DividerItemDecoration(context, VERTICAL)
            addItemDecoration(dividerItemDecoration)
            adapter = this@SearchTasksFragment.adapter
        }
    }

    override fun onResume() {
        super.onResume()
        loadFilteredTask()
    }

    private fun loadFilteredTask() {
        val filtersData = getFilterDataFromSheet()
        viewModel.loadTasksWithFilters(filtersData)
    }

    private fun getFilterDataFromSheet(): FilterTaskDataModel {
        with(binding) {
            return FilterTaskDataModel(
                if (titleEditText.isTextBlank()) "" else titleEditText.text(),
                if (descriptionEditText.isTextBlank()) "" else descriptionEditText.text(),
                if (startDateEditText.isTextBlank()) 0 else dateToTimestamp(startDateEditText.text()),
                if (endDateEditText.isTextBlank()) Long.MAX_VALUE else dateToTimestamp(endDateEditText.text())
            )
        }
    }

    private fun AppCompatAutoCompleteTextView.getParamValueFromSpinner(): Boolean? {
        if (this.isTextBlank()) return null
        if (this.text.toString() == "true") return true
        return false
    }

    private fun showFoundTasksNumberToast() {
        when (adapter.itemCount) {
            0 -> toast.setText("No results found")
            1 -> toast.setText("1 result found")
            else -> toast.setText("${adapter.itemCount} results found")
        }
        toast.show()
    }

    private fun setupDatePicking() {
        binding.startDateEditText.setOnClickListener {
            hideKeyboard(context!!, binding.root)
            binding.startDateEditText.error = null
            openPickDateDialog(context!!, binding.startDateEditText)
        }
        binding.startDateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideKeyboard(context!!, binding.root)
                binding.startDateEditText.callOnClick()
            }
        }
        binding.endDateEditText.setOnClickListener {
            hideKeyboard(context!!, binding.root)
            binding.endDateEditText.error = null
            openPickDateDialog(context!!, binding.endDateEditText)
        }
        binding.endDateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideKeyboard(context!!, binding.root)
                binding.endDateEditText.callOnClick()
            }
        }
    }
}