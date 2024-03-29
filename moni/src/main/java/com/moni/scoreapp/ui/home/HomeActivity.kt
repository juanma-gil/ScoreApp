package com.moni.scoreapp.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import com.moni.scoreapp.R
import com.moni.scoreapp.ui.home.fragments.record.RecordsFragment
import com.moni.scoreapp.databinding.ActivityHomeBinding
import com.moni.scoreapp.databinding.CustomToolbarBinding
import com.moni.scoreapp.ui.home.fragments.ApprovedFragment
import com.moni.scoreapp.ui.home.fragments.RejectedFragment
import com.moni.scoreapp.ui.home.fragments.ScorerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var toolbarBinding: CustomToolbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        toolbarBinding = CustomToolbarBinding.bind(binding.root)
        setContentView(binding.root)
        setSupportActionBar(toolbarBinding.topAppBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val scorerFragment = ScorerFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.home_fragment_container, scorerFragment).commit()

        val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> goToScorerFragment()
                    1 -> goToRecordFragment()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Do nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Do nothing
            }
        }
        binding.customToolbar.tabLayout.addOnTabSelectedListener(tabSelectedListener)
    }

    fun goToScorerFragment() {
        val scorerFragment = ScorerFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.home_fragment_container, scorerFragment).commit()
    }

    fun goToRecordFragment() {
        val recordsFragment = RecordsFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.home_fragment_container, recordsFragment).commit()
    }

    fun goToApprovedFragment() {
        val approvedFragment = ApprovedFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.home_fragment_container, approvedFragment).commit()
    }

    fun goToRejectedFragment() {
        val rejectedFragment = RejectedFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.home_fragment_container, rejectedFragment).commit()
    }
}