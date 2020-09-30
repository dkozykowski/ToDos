package com.gmail.dkozykowski.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
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
import com.gmail.dkozykowski.model.ActionBarButtonModel
import com.gmail.dkozykowski.ui.activity.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

class ViewPagerFragment : BaseFragment() {
    private lateinit var binding: FragmentViewPagerBinding
    private val pages = arrayOf(
        TodaysTasksFragment(::updateIdlePage),
        ActiveTasksFragment(::updateIdlePage),
        DoneTasksFragment(::updateIdlePage)
    )
    private val titles = arrayOf("Today's", "Active", "Done")
    override var leftActionBarButtonHandler: ActionBarButtonModel? = null
    override var rightActionBarButtonHandler: ActionBarButtonModel? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_pager, container, false)
        setupActionBarButtonsHandlers()
        setupViewPager()
        setupLeftActionBarButton()
        setupRightActionBarButton()
        return binding.root
    }

    private fun setupActionBarButtonsHandlers() {
        leftActionBarButtonHandler = (activity as MainActivity).leftActionBarButton
        rightActionBarButtonHandler = (activity as MainActivity).rightActionBarButton
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int): Fragment = pages[position]
            override fun getCount() = pages.size
            override fun getPageTitle(position: Int): CharSequence? = titles[position]
        }
        binding.viewPager.setPageTransformer(true, ScaleInOutTransformer())
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun setupLeftActionBarButton() {
        leftActionBarButtonHandler?.setVisible(true)
        leftActionBarButtonHandler?.setIcon(R.drawable.ic_baseline_post_add_24)
        leftActionBarButtonHandler?.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_NewTaskFragment)
        }
    }

    private fun setupRightActionBarButton() {
        rightActionBarButtonHandler?.setVisible(true)
        rightActionBarButtonHandler?.setIcon(R.drawable.ic_round_search_24)
        rightActionBarButtonHandler?.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_searchTasksFragment)
        }
    }

    override fun onBackPressed() {
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
            TODAYS -> pages[0].reloadTasks()
            ALL_ACTIVE -> pages[1].reloadTasks()
            DONE -> pages[2].reloadTasks()
            else -> {
            }
        }
    }
}