package com.gmail.applicationtodos.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.gmail.applicationtodos.QueryTaskType.SEARCH
import com.gmail.applicationtodos.R
import com.gmail.applicationtodos.databinding.FragmentSearchTasksBinding
import com.gmail.applicationtodos.model.ActionBarButtonModel
import com.gmail.applicationtodos.model.FilterTaskDataModel
import com.gmail.applicationtodos.ui.activity.MainActivity
import com.gmail.applicationtodos.ui.adapter.TaskAdapter
import com.gmail.applicationtodos.ui.fragment.SearchTasksFragment.LoadingState.AWAITING
import com.gmail.applicationtodos.ui.fragment.SearchTasksFragment.LoadingState.DONE
import com.gmail.applicationtodos.utils.*
import com.gmail.applicationtodos.viewmodel.TaskViewModel
import com.gmail.applicationtodos.viewmodel.TaskViewModel.LoadViewState.*

class SearchTasksFragment : BaseFragment() {
    private lateinit var binding: FragmentSearchTasksBinding
    private val adapter by lazy { TaskAdapter(SEARCH, ::showEmptyInfo) }
    private var areFiltersShown = false
    private lateinit var toast: Toast
    override var leftActionBarButtonHandler: ActionBarButtonModel? = null
    override var rightActionBarButtonHandler: ActionBarButtonModel? = null
    private var searchButtonResultToastState = DONE
    private val viewModel by lazy { ViewModelProvider(
        this,
        viewModelFactory { TaskViewModel(SEARCH) }
    ).get(TaskViewModel::class.java) }
    enum class LoadingState { AWAITING, DONE }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchTasksBinding.inflate(inflater, container, false)
        loadSetupFunctions()
        return binding.root
    }

    private fun loadSetupFunctions() {
        setupActionBarButtonsHandlers()
        setupToast()
        setupAdapter()
        setupViewModel()
        setupLoadTaskLiveDataObserver()
        setupButtons()
        setupDatePicking()
        setupLeftActionBarButton()
        setupRightActionBarButton()
    }

    private fun setupActionBarButtonsHandlers() {
        leftActionBarButtonHandler = (activity as MainActivity).leftActionBarButton
        rightActionBarButtonHandler = (activity as MainActivity).rightActionBarButton
    }

    private fun setupToast() {
        toast = Toast.makeText(context!!, "", Toast.LENGTH_SHORT)
    }

    private fun setupAdapter() {
        adapter.context = context!!
    }

    private fun setupViewModel() {
        viewModel.context = context!!
    }

    private fun setupButtons() {
        setupRootOnClickEvent()
        setupSearchButton()
        setupUpdateFiltersViewButton()
        setupRemoveDateButtons()
    }

    private fun setupRootOnClickEvent() {
        binding.root.setOnClickListener { hideKeyboard(context!!, binding.root) }
    }

    private fun setupSearchButton() {
        binding.searchButton.setOnClickListener {
            searchButtonResultToastState = AWAITING
            loadFilteredTask()
        }
    }

    private fun setupUpdateFiltersViewButton() {
        binding.updateFiltersViewButton.setOnClickListener {
            areFiltersShown = !areFiltersShown
            binding.filtersLayout.visibility = if (areFiltersShown) VISIBLE else GONE
            animateUpdateFiltersViewButtonRotation()
        }
    }

    private fun setupRemoveDateButtons() {
        binding.removeEndDateButton.setOnClickListener {
            binding.endDateEditText.text = null
        }
        binding.removeStartDateButton.setOnClickListener {
            binding.startDateEditText.text = null
        }
    }

    private fun animateUpdateFiltersViewButtonRotation() {
        binding.updateFiltersViewButton.animate().rotation(if (areFiltersShown) 180F else 0F)
            .setDuration(if (areFiltersShown) 350 else 600).start()
    }

    private fun setupLoadTaskLiveDataObserver() {
        viewModel.loadTaskLiveData.observe(viewLifecycleOwner, Observer { viewState ->
           if (viewState is Success) {
               adapter.updateData(viewState.data)
               updateEmptyInfo()
               showSearchButtonResultAwaitingToast()
           }
        })
    }

    override fun onBackPressed() {
        hideKeyboard(context!!, binding.root)
        findNavController().navigateUp()
    }

    private fun showEmptyInfo() {
        binding.emptyListText.setAsVisible()
    }

    private fun updateEmptyInfo() {
        when (adapter.isDataEmpty()) {
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
                if (endDateEditText.isTextBlank()) Long.MAX_VALUE else dateToTimestamp(
                    endDateEditText.text()
                )
            )
        }
    }

    private fun showSearchButtonResultAwaitingToast() {
        if (searchButtonResultToastState == AWAITING) {
            showResultNumberToast()
            searchButtonResultToastState = DONE
        }
    }

    private fun showResultNumberToast() {
        when (adapter.itemCount) {
            0 -> toast.setText("No results found")
            1 -> toast.setText("1 result found")
            else -> toast.setText("${adapter.itemCount} results found")
        }
        toast.show()
    }

    private fun setupDatePicking() {
        setupLowerboundDatePicking()
        setupUpperboundDatePicking()
    }

    private fun setupLeftActionBarButton() {
        leftActionBarButtonHandler?.setVisible(false)
    }

    private fun setupRightActionBarButton() {
        rightActionBarButtonHandler?.setVisible(true)
        rightActionBarButtonHandler?.setIcon(R.drawable.ic_baseline_arrow_back_24)
        rightActionBarButtonHandler?.setOnClickListener { onBackPressed() }
    }

    private fun setupLowerboundDatePicking() {
        binding.startDateEditText.setOnClickListener {
            binding.startDateEditText.preventDoubleClick()
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
    }

    private fun setupUpperboundDatePicking() {
        binding.endDateEditText.setOnClickListener {
            binding.endDateEditText.preventDoubleClick()
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