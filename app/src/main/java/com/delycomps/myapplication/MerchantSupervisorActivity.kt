package com.delycomps.myapplication

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.delycomps.myapplication.databinding.ActivityPromoterBinding
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.ui.promoter.SectionsPagerAdapter
import com.delycomps.myapplication.ui.supervisor.MerchantSectionsPagerAdapter

class MerchantSupervisorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromoterBinding
    private lateinit var tabs: TabLayout
    private lateinit var pointSale: PointSale
    private lateinit var supervisorViewModel: SupervisorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supervisorViewModel = ViewModelProvider(this).get(SupervisorViewModel::class.java)

        binding = ActivityPromoterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pointSale = intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        val sectionsPagerAdapter = MerchantSectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.promoterViewPager
        viewPager.adapter = sectionsPagerAdapter
        tabs = binding.promoterTabs
        tabs.setupWithViewPager(viewPager)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = pointSale.client

    }
}