package com.delycomps.rintisa

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.databinding.ActivityPromoterBinding
import com.delycomps.rintisa.model.DataSupervisor
import com.delycomps.rintisa.model.PointSale
import com.delycomps.rintisa.ui.supervisor.MerchantSectionsPagerAdapter
import com.google.gson.Gson

class MerchantSupervisorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromoterBinding
    private lateinit var tabs: TabLayout
    private lateinit var pointSale: PointSale
    private lateinit var supervisorViewModel: SupervisorViewModel

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val output = Intent()
                output.putExtra("status", "INICIADO")
                setResult(RESULT_OK, output)
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
    override fun onBackPressed() {
        val output = Intent()
        output.putExtra("status", "INICIADO")
        setResult(RESULT_OK, output)
        finish()
    }
}