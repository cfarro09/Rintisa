package com.delycomps.rintisa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.databinding.ActivityPromoterBinding
import com.delycomps.rintisa.model.Customer
import com.delycomps.rintisa.model.DataAuditor
import com.delycomps.rintisa.model.DataAuditorRinti
import com.delycomps.rintisa.model.UserFromAuditor
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson

class AuditorRintiDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromoterBinding
    private lateinit var tabs: TabLayout
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

        val type = intent.getStringExtra("TYPE") ?: ""

        val jsonMerchant = SharedPrefsCache(this).get("data-auditorrinti", "string")
        val dataSupervisor = Gson().fromJson(jsonMerchant.toString(), DataAuditorRinti::class.java)
        auditorViewModel.setMultiInitialRinti(dataSupervisor)

        val sectionsPagerAdapter = com.delycomps.rintisa.ui.auditor.SectionsPagerAdapter(this, supportFragmentManager, type)
        val viewPager: ViewPager = binding.promoterViewPager
        viewPager.adapter = sectionsPagerAdapter
        tabs = binding.promoterTabs
        tabs.setupWithViewPager(viewPager)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (type == "CLIENT") {
            val customer: Customer = intent.getParcelableExtra(Constants.POINT_CUSTOMER)!!
            supportActionBar?.title = customer.client
        } else {
            val customer: UserFromAuditor = intent.getParcelableExtra(Constants.POINT_CUSTOMER)!!
            supportActionBar?.title = customer.description
        }
    }

    override fun onBackPressed() {
        val output = Intent()
        if (auditorViewModel.resExecute.value != null) {
            if (auditorViewModel.resExecute.value?.result == "REGISTRADO") {
                output.putExtra("status", "GESTIONADO")
            } else {
                output.putExtra("status", "EN ESPERA")
            }
        }
        setResult(RESULT_OK, output);
        finish()
    }
}