package com.delycomps.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.ui.supervisorPromoter.SectionsPagerAdapter
import com.delycomps.myapplication.databinding.ActivityPromoterSupervisorBinding
import com.delycomps.myapplication.model.DataSupervisor
import com.delycomps.myapplication.model.PointSale
import com.google.gson.Gson

class PromoterSupervisorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPromoterSupervisorBinding
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

        pointSale = intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        binding = ActivityPromoterSupervisorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jsonMerchant = SharedPrefsCache(this).get("data-supervisor", "string")
        val dataSupervisor = Gson().fromJson(jsonMerchant.toString(), DataSupervisor::class.java)
        supervisorViewModel.setMultiInitial(dataSupervisor)

        supervisorViewModel.setUserSelected(pointSale.userid ?: 0)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
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