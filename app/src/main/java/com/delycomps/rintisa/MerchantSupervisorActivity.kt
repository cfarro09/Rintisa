package com.delycomps.rintisa

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.delycomps.rintisa.adapter.AdapterQuestions
import com.delycomps.rintisa.cache.BDLocal
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.databinding.ActivityPromoterBinding
import com.delycomps.rintisa.model.DataSupervisor
import com.delycomps.rintisa.model.PointSale
import com.delycomps.rintisa.ui.supervisor.MerchantSectionsPagerAdapter
import com.google.gson.Gson
import org.json.JSONObject

class MerchantSupervisorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromoterBinding
    private lateinit var tabs: TabLayout
    private lateinit var pointSale: PointSale
    private lateinit var supervisorViewModel: SupervisorViewModel
    private lateinit var dialogLoading: AlertDialog

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_one -> {
                val comment = supervisorViewModel.comment.value ?: ""
                val auditJson = supervisorViewModel.auditJson.value ?: "[]"

                val userid = SharedPrefsCache(this).get("userid", "string").toString().toInt()
                val visitSup = BDLocal(this).getVisitSupervisor(pointSale.userid ?: 0, pointSale.customerId, userid)

                visitSup!!.comment = comment
                visitSup.auditjson = auditJson
                visitSup.status = null

                if (visitSup.createDate != "") {
                    visitSup.status = "PORENVIAR"
                    BDLocal(this).updateMerchantSupervisor(visitSup)
                    Toast.makeText(this, "El inicio no fue registrado, procesa a guardar cuando tenga internet", Toast.LENGTH_LONG).show()

                    val output = Intent()
                    output.putExtra("status", "GESTIONADO")
                    setResult(RESULT_OK, output)
                    finish()
                    return true
                }


                BDLocal(this).updateMerchantSupervisor(visitSup)

                val ob = JSONObject()
                ob.put("auditdetail", auditJson)
                ob.put("customerid", pointSale.customerId)
                ob.put("image1", visitSup.image1)
                ob.put("initdate", visitSup.createDate)
                ob.put("latitude", visitSup.latitude)
                ob.put("longitude", visitSup.longitude)
                ob.put("aux_userid", visitSup.userId)
                ob.put("type", visitSup.type)
                ob.put("visitid", pointSale.visitId)
                ob.put("comment", comment)

                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            dialogLoading.show()
                            supervisorViewModel.executeSupervisor(ob, "UFN_MANAGE_SUPERVISOR_MERCHANT_OFF", SharedPrefsCache(this).getToken())
                        }
                    }
                }
                val builder = AlertDialog.Builder(this)
                builder.setMessage("¿Está seguro de enviar la auditoria??")
                    .setPositiveButton(Html.fromHtml("<b>Continuar<b>"), dialogClickListener)
                    .setNegativeButton(Html.fromHtml("<b>Cancelar<b>"), dialogClickListener)
                val alert = builder.create()
                alert.show()

            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supervisorViewModel = ViewModelProvider(this).get(SupervisorViewModel::class.java)

        binding = ActivityPromoterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

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

        supervisorViewModel.resExecute.observe(this) {
            if ((it.result) == "UFN_MANAGE_SUPERVISOR_MERCHANT_OFF") {
                if (!it.loading && it.success) {
                    val userid = SharedPrefsCache(this).get("userid", "string").toString().toInt()
                    val visitSup = BDLocal(this).getVisitSupervisor(pointSale.userid ?: 0, pointSale.customerId, userid)
                    BDLocal(this).deleteSupervisorVisit(visitSup!!.uuid!!)
                    dialogLoading.dismiss()
                    Toast.makeText(this, "Auditoria registrada correctamente", Toast.LENGTH_LONG).show()
                    supervisorViewModel.initExecute()
                } else if (!it.loading && !it.success) {
                    val userid = SharedPrefsCache(this).get("userid", "string").toString().toInt()
                    val visitSup = BDLocal(this).getVisitSupervisor(pointSale.userid ?: 0, pointSale.customerId, userid)
                    visitSup?.comment = null
                    visitSup?.auditjson = null
                    visitSup?.status = "PORENVIAR"
                    BDLocal(this).updateMerchantSupervisor(visitSup!!)
                    dialogLoading.dismiss()
                    Toast.makeText(this, "Hay problema de conexión, cuando haya internet proceder a volver a guardar", Toast.LENGTH_LONG).show()
                }
                val output = Intent()
                output.putExtra("status", "GESTIONADO")
                setResult(RESULT_OK, output)
                finish()
            }
        }
    }
    override fun onBackPressed() {
        val output = Intent()
        output.putExtra("status", "INICIADO")
        setResult(RESULT_OK, output)
        finish()
    }
}