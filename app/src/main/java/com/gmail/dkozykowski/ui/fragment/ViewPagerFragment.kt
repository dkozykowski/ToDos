package com.gmail.dkozykowski.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import com.ToxicBakery.viewpager.transforms.ScaleInOutTransformer
import com.gmail.dkozykowski.QueryTaskType
import com.gmail.dkozykowski.QueryTaskType.*
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.databinding.FragmentViewPagerBinding
import kotlin.system.exitProcess

class ViewPagerFragment : Fragment() {
    private lateinit var binding: FragmentViewPagerBinding
    private val pages = arrayOf(
        TodaysTasksFragment(::updateIdlePage),
        ActiveTasksFragment(::updateIdlePage),
        DoneTasksFragment(::updateIdlePage)
    )
    private val titles = arrayOf("Today's", "Active", "Done")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_pager, container, false)
        binding.viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int): Fragment = pages[position]

            override fun getCount() = pages.size

            override fun getPageTitle(position: Int): CharSequence? = titles[position]
        }

        binding.viewPager.setPageTransformer(true, ScaleInOutTransformer())

        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.newTaskButton.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_NewTaskFragment)
        }

        binding.searchButton.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_searchTasksFragment)
        }

        return binding.root
    }

    fun onBackPressed() {
        AlertDialog.Builder(context!!).apply {
            setMessage("Close application?")
            setPositiveButton("Yes") { _, _ ->
                activity!!.finish()
                exitProcess(0)
            }
            setNegativeButton("No", null)
        }.show()
    }

    private fun updateIdlePage(fragmentType: QueryTaskType) {
        when (fragmentType) {
            TODAYS -> (pages[0] as TodaysTasksFragment).viewModel.loadTasks(TODAYS)
            ALL_ACTIVE -> (pages[1] as ActiveTasksFragment).viewModel.loadTasks(ALL_ACTIVE)
            DONE -> (pages[2] as DoneTasksFragment).viewModel.loadTasks(DONE)
            else -> {}
        }
    }
}