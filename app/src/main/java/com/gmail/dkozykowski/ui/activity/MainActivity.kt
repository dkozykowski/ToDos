package com.gmail.dkozykowski.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.gmail.dkozykowski.R
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.databinding.ActivityMainBinding
import com.gmail.dkozykowski.ui.fragment.NewTaskFragment
import com.gmail.dkozykowski.ui.fragment.PreviewTaskFragment
import com.gmail.dkozykowski.ui.fragment.SearchTasksFragment
import com.gmail.dkozykowski.ui.fragment.ViewPagerFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DB.createDatabase(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
    }

    override fun onBackPressed() {
        val nav = findNavController(R.id.nav_host_fragment)
        val currentDest = nav.currentDestination
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        when (currentDest?.id) {
            R.id.viewPagerFragment -> {
                val fragment = navHostFragment!!.childFragmentManager.fragments[0] as ViewPagerFragment
                fragment.onBackPressed()
            }
            R.id.previewTaskFragment -> {
                val fragment = navHostFragment!!.childFragmentManager.fragments[0] as PreviewTaskFragment
                fragment.onBackPressed()
            }
            R.id.newTaskFragment -> {
                val fragment = navHostFragment!!.childFragmentManager.fragments[0] as NewTaskFragment
                fragment.onBackPressed()
            }
            R.id.searchTasksFragment -> {
                val fragment = navHostFragment!!.childFragmentManager.fragments[0] as SearchTasksFragment
                fragment.onBackPressed()
            }
        }
    }
}


