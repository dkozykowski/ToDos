package com.gmail.dkozykowski.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.databinding.FragmentViewPagerBinding

class ViewPagerFragment : Fragment() {
    private lateinit var binding: FragmentViewPagerBinding
    private val pages = arrayOf(
            ActiveTasksFragment(),
            ImportantTasksFragment(),
            DoneTasksFragment()
        )
    private val titles = arrayOf("Active", "Important", "Done")

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

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                pages[position].onResume()
            }
        })

        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.newTaskButton.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_NewTaskFragment)
        }
        return binding.root
    }
}