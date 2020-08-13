package com.gmail.dkozykowski.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.databinding.ActivityMainBinding
import com.gmail.dkozykowski.ui.fragment.ActiveTasksFragment
import com.gmail.dkozykowski.ui.fragment.DoneTasksFragment
import com.gmail.dkozykowski.ui.fragment.ImportantTasksFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val pages =
        arrayOf(
            ActiveTasksFragment(),
            ImportantTasksFragment(),
            DoneTasksFragment()
        )
    private val titles = arrayOf("Active", "Important", "Done")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DB.createDatabase(this)

        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )
        binding.viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
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
            ) {}

            override fun onPageSelected(position: Int) {
                pages[position].onResume()
            }
        })

        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.newTaskButton.setOnClickListener {
            val intent = Intent(this, NewTaskActivity::class.java)
            startActivity(intent)
        }
    }
}