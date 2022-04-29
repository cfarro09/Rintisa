package com.delycomps.myapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.databinding.ActivityPromoterBinding
import com.delycomps.myapplication.model.DataSupervisor
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.ui.promoter.SectionsPagerAdapter
import com.delycomps.myapplication.ui.supervisor.MerchantSectionsPagerAdapter
import com.google.gson.Gson

class MerchantSupervisorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromoterBinding
    private lateinit var tabs: TabLayout
    private lateinit var pointSale: PointSale
    private lateinit var supervisorViewModel: SupervisorViewModel

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supervisorViewModel = ViewModelProvider(this).get(SupervisorViewModel::class.java)

        binding = ActivityPromoterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pointSale = intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        supervisorViewModel.getMaterials(pointSale.visitId, SharedPrefsCache(this).getToken())

        val jsonMerchant = SharedPrefsCache(this).get("data-supervisor", "string")
        val dataSupervisor = Gson().fromJson(jsonMerchant.toString(), DataSupervisor::class.java)
        supervisorViewModel.setMultiInitial(dataSupervisor)

        val sectionsPagerAdapter = MerchantSectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.promoterViewPager
        viewPager.adapter = sectionsPagerAdapter
        tabs = binding.promoterTabs
        tabs.setupWithViewPager(viewPager)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = pointSale.client
    }
}