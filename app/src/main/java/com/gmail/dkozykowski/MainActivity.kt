package com.gmail.dkozykowski

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.gmail.dkozykowski.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val pages =
        arrayOf(ActiveTasksFragment(), ImportantTasksFragment(), DoneTasksFragment())
    private val titles = arrayOf("Active", "Important", "Done")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment = pages[position]

            override fun getCount() = pages.size

            override fun getPageTitle(position: Int): CharSequence? = titles[position]
        }
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}