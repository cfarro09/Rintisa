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
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.delycomps.rintisa.adapter.AdapterPointsale
import com.delycomps.rintisa.cache.BDLocal
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.ui.supervisorPromoter.SectionsPagerAdapter
import com.delycomps.rintisa.databinding.ActivityPromoterSupervisorBinding
import com.delycomps.rintisa.model.DataSupervisor
import com.delycomps.rintisa.model.PointSale
import com.delycomps.rintisa.model.VisitSupervisor
import com.delycomps.rintisa.model.resImage
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PromoterSupervisorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPromoterSupervisorBinding
    private lateinit var pointSale: PointSale
    private lateinit var supervisorViewModel: SupervisorViewModel
    private lateinit var dialogLoading: AlertDialog
    private lateinit var visitSup: VisitSupervisor
    private var listImages: MutableList<resImage> = arrayListOf()
    private var imageIndex: Int = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val output = Intent()
                output.putExtra("status", "INICIADO")
                setResult(RESULT_OK, output)
                finish()
                return true
            }
            R.id.action_one -> {
                val comment = supervisorViewModel.comment.value ?: ""
                val image2 = supervisorViewModel.image2.value ?: ""
                val image3 = supervisorViewModel.image3.value ?: ""
                val image4 = supervisorViewModel.image4.value ?: ""
                val image5 = supervisorViewModel.image5.value ?: ""
                val speechRCN = supervisorViewModel.speechRCN.value ?: ""
                val speechRCT = supervisorViewModel.speechRCT.value ?: ""
                val speechSCN = supervisorViewModel.speechSCN.value ?: ""
                val speechSCT = supervisorViewModel.speechSCT.value ?: ""
                val uniformJson = supervisorViewModel.uniformJson.value ?: "[]"
                val materialJson = supervisorViewModel.materialJson.value ?: "[]"
                val statusJson = supervisorViewModel.statusJson.value ?: "[]"
                val userSelected = supervisorViewModel.userSelected.value ?: 0

                val userid = SharedPrefsCache(this).get("userid", "string").toString().toInt()
                visitSup = BDLocal(this).getVisitSupervisor(pointSale.userid ?: 0, pointSale.customerId, userid)!!

                visitSup.comment = comment
                visitSup.status = null
                visitSup.image2 = image2
                visitSup.image3 = image3
                visitSup.image4 = image4
                visitSup.image5 = image5
                visitSup.speechRCN = speechRCN
                visitSup.speechRCT = speechRCT
                visitSup.speechSCN = speechSCN
                visitSup.speechSCT = speechSCT
                visitSup.uniformJson = uniformJson
                visitSup.materialJson = materialJson
                visitSup.statusJson = statusJson
                visitSup.userIdSelected = userSelected

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

                //validate if have to upload images
                if (image2 != "") listImages.add(resImage("image2", image2, null))
                if (image3 != "") listImages.add(resImage("image3", image3, null))
                if (image4 != "") listImages.add(resImage("image4", image4, null))
                if (image5 != "") listImages.add(resImage("image5", image5, null))

                dialogLoading.show()
                if (listImages.isEmpty()) {
                    triggerInfo()
                } else {
                    imageIndex = 0
                    supervisorViewModel.uploadWithBD(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
                }
            }
        }
        return false
    }

    private fun triggerInfo() {
        val ob = JSONObject()
        ob.put("image2", visitSup.image2)
        ob.put("image3", visitSup.image3)
        ob.put("image4", visitSup.image4)
        ob.put("image5", visitSup.image5)
        ob.put("speechrcn", visitSup.speechRCN)
        ob.put("speechrct", visitSup.speechRCT)
        ob.put("speechscn", visitSup.speechSCN)
        ob.put("speechsct", visitSup.speechSCT)
        ob.put("uniformjson", visitSup.uniformJson)
        ob.put("materialjson", visitSup.materialJson)
        ob.put("statusjson", visitSup.statusJson)
        ob.put("useridselected", visitSup.userIdSelected)
        ob.put("customerid", pointSale.customerId)
        ob.put("image1", visitSup.image1)
        ob.put("initdate", visitSup.createDate)
        ob.put("latitude", visitSup.latitude)
        ob.put("longitude", visitSup.longitude)
        ob.put("aux_userid", visitSup.userId)
        ob.put("type", visitSup.type)
        ob.put("visitid", pointSale.visitId)
        ob.put("comment", visitSup.comment)

        supervisorViewModel.executeSupervisor(ob, "UFN_MANAGE_SUPERVISOR_PROMOTER_OFF", SharedPrefsCache(this).getToken())
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

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = pointSale.client

        supervisorViewModel.urlImageWithBD.observe(this) {
            if (!it.loading) {
                if (it.result == "") {
                    finishVisitFail()
                } else {
                    when (listImages[imageIndex].type) {
                        "image1" -> visitSup.image1 = it.result
                        "image2" -> visitSup.image2 = it.result
                        "image3" -> visitSup.image3 = it.result
                        "image4" -> visitSup.image4 = it.result
                        "image5" -> visitSup.image5 = it.result
                    }
                    imageIndex++
                    if (imageIndex == listImages.count()) {
                        triggerInfo()
                    } else {
                        supervisorViewModel.uploadWithBD(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
                    }
                }
            }
        }

        supervisorViewModel.resExecute.observe(this) {
            if ((it.result) == "UFN_MANAGE_SUPERVISOR_PROMOTER_OFF") {
                if (!it.loading && it.success) {
                    BDLocal(this).deleteSupervisorVisit(visitSup.uuid!!)
                    Toast.makeText(this, "Se envió correctamente", Toast.LENGTH_LONG).show()
                    supervisorViewModel.initExecute()
                    finishVisitFail(true)
                } else if (!it.loading && !it.success) {
                    finishVisitFail()
                }
            }
        }

    }

    private fun finishVisitFail (onlyFinish: Boolean = false) {
        dialogLoading.dismiss()
        if (!onlyFinish) {
            visitSup.status = "PORENVIAR"
            BDLocal(this).updateMerchantSupervisor(visitSup)
            Toast.makeText(this, "El inicio no fue registrado, procesa a guardar cuando tenga internet", Toast.LENGTH_LONG).show()
        }

        val output = Intent()
        output.putExtra("status", "GESTIONADO")
        setResult(RESULT_OK, output)
        finish()
    }
    override fun onBackPressed() {
        val output = Intent()
        output.putExtra("status", "INICIADO")
        setResult(RESULT_OK, output)
        finish()
    }
}