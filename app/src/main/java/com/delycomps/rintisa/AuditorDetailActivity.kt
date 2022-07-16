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
import com.delycomps.rintisa.model.Customer
import com.delycomps.rintisa.model.DataAuditor
import com.google.gson.Gson

class AuditorDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromoterBinding
    private lateinit var tabs: TabLayout
    private lateinit var customer: Customer
    private lateinit var auditorViewModel: AuditorViewModel

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val output = Intent()
                if (auditorViewModel.resExecute.value != null) {
                    if (auditorViewModel.resExecute.value?.result == "REGISTRADO") {
                        output.putExtra("status", "GESTIONADO")
                    } else {
                        output.putExtra("status", "EN ESPERA")
                    }
                } else {
                    output.putExtra("status", "GESTIONADO")
                }
                setResult(RESULT_OK, output);
                finish()
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auditorViewModel = ViewModelProvider(this).get(AuditorViewModel::class.java)

        binding = ActivityPromoterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customer = intent.getParcelableExtra(Constants.POINT_CUSTOMER)!!

        val jsonMerchant = SharedPrefsCache(this).get("data-auditor", "string")
        val dataSupervisor = Gson().fromJson(jsonMerchant.toString(), DataAuditor::class.java)
        auditorViewModel.setMultiInitial(dataSupervisor)

        val sectionsPagerAdapter = com.delycomps.rintisa.ui.auditor.SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.promoterViewPager
        viewPager.adapter = sectionsPagerAdapter
        tabs = binding.promoterTabs
        tabs.setupWithViewPager(viewPager)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = customer.client
    }

    override fun onBackPressed() {
        val output = Intent()
        if (auditorViewModel.resExecute.value != null) {
            if (auditorViewModel.resExecute.value?.result == "REGISTRADO") {
                output.putExtra("status", "GESTIONADO")
            } else {
                output.putExtra("status", "EN ESPERA")
            }
        } else {
            output.putExtra("status", "GESTIONADO")
        }
        setResult(RESULT_OK, output);
        finish()
    }
}