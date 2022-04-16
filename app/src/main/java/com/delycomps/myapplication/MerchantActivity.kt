package com.delycomps.myapplication

import android.app.AlertDialog
import android.app.Fragment
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.delycomps.myapplication.Constants.RETURN_ACTIVITY
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.databinding.ActivityMerchantBinding
import com.delycomps.myapplication.model.Availability
import com.delycomps.myapplication.model.DataMerchant
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.ui.merchant.InformationFragment
import com.delycomps.myapplication.ui.merchant.MerchantViewModel
import com.delycomps.myapplication.ui.merchant.SectionsPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class MerchantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantBinding
    private lateinit var merchantViewModel: MerchantViewModel
    private lateinit var pointSale: PointSale
    private lateinit var dialogLoading: AlertDialog
    private lateinit var tabs: TabLayout

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.action_one) {
            val imageBefore = merchantViewModel.urlBeforeImage.value ?: ""
            val imageAfter = merchantViewModel.urlAfterImage.value ?: ""
            val listMaterials = merchantViewModel.listMaterialSelected.value ?: emptyList()
            val listProducts = merchantViewModel.listProductSelected.value ?: emptyList()

            val datecurrent = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
//            val listAvailabilities =

            val listAvailabilitiesProcessed = (merchantViewModel.dataProducts.value ?: emptyList())
                .map { mapOf<String, Any>(
                    "customerid" to pointSale.customerId,
                    "description_availability" to datecurrent,
                    "status_availability" to "ACTIVO",
                    "type_availability" to "NINGUNO",
                    "availabilitydetailid" to 0,
                    "productid" to it.productId,
                    "brand" to it.brand + "",
                    "competence" to it.competence + "",
                    "description_availabilitydetail" to it.description + "",
                    "status_availabilitydetail" to "ACTIVO",
                    "type_availabilitydetail" to "NINGUNO",
                    "operation" to "INSERT",
                    "flag_availabilitydetail" to (merchantViewModel.productsAvailability.value ?: emptyList()).any { r -> r.productid == it.productId }
                ) }.toList()

            val statusManagement = merchantViewModel.management.value?.status_management ?: "EFECTIVA"
            val motive = merchantViewModel.management.value?.motive ?: ""
            val observation = merchantViewModel.management.value?.observation ?: ""

            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialogLoading.show()
                        val listMaterialProcessed = listMaterials.map { mapOf<String, Any>(
                            "material_visitid" to 0,
                            "visitid" to pointSale.visitId,
                            "quantity" to it.quantity,
                            "description" to it.material.toString(),
                            "brand" to it.brand.toString(),
                            "status" to "ACTIVO",
                            "type" to "NINGUNO",
                            "operation" to "INSERT",
                        ) }.toList()
                        val listProductProcessed = listProducts.map { mapOf<String, Any>(
                            "pricesurveyid" to 0,
                            "customerid" to pointSale.customerId,
                            "description_pricesurvey" to datecurrent,
                            "status_pricesurvey" to "ACTIVO",
                            "type_pricesurvey" to "NINGUNO",
                            "pricesurveydetailid" to 0,
                            "productid" to it.productId,
                            "measure_unit" to it.measureUnit.toString(),
                            "price" to it.price,
                            "quantity" to 0,
                            "description_pricesurveydetail" to it.description.toString(),
                            "status_pricesurveydetail" to "ACTIVO",
                            "type_pricesurveydetail" to "NINGUNO",
                            "operation" to "INSERT"
                        ) }.toList()
                        merchantViewModel.closeMerchant(
                            pointSale.visitId,
                            imageBefore,
                            imageAfter,
                            Gson().toJson(listMaterialProcessed),
                            Gson().toJson(listProductProcessed),
                            listProductProcessed.count() > 0,
                            Gson().toJson(listAvailabilitiesProcessed),
                            (merchantViewModel.productsAvailability.value ?: emptyList()).count() > 0,
                            statusManagement, motive, observation, SharedPrefsCache(this).getToken())
                    }
                }
            }
            val builder = AlertDialog.Builder(this)
            builder.setMessage("¿Está seguro de cerrar el punto de venta?")
                .setPositiveButton(Html.fromHtml("<b>Continuar<b>"), dialogClickListener)
                .setNegativeButton(Html.fromHtml("<b>Cancelar<b>"), dialogClickListener)
            val alert = builder.create()
            alert.show()

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        merchantViewModel = ViewModelProvider(this).get(MerchantViewModel::class.java)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        val jsonMerchant = SharedPrefsCache(this).get("data-merchant", "string")
        val dataMerchant = Gson().fromJson(jsonMerchant.toString(), DataMerchant::class.java)
        merchantViewModel.initMainMulti(dataMerchant)

        merchantViewModel.closingMerchant.observe(this) {
            dialogLoading.dismiss()
            if (it) {
                Toast.makeText(this, "Se actualizó el punto de venta", Toast.LENGTH_LONG).show()
                BDLocal(this).deleteMerchantPricesFromVisit(pointSale.visitId)
                BDLocal(this).deleteMaterialFromVisit(pointSale.visitId)
                BDLocal(this).deleteProductAvailabilityFromVisit(pointSale.visitId)

                val output = Intent()
                output.putExtra("status", "VISITADO")
                setResult(RESULT_OK, output);
                finish()
            } else {
                Toast.makeText(this, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show()
            }
        }

        binding = ActivityMerchantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pointSale = intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, pointSale.showSurvey, pointSale.showAvailability)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        tabs = binding.tabs
        tabs.setupWithViewPager(viewPager)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = pointSale.client
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        val output = Intent()
        output.putExtra("status", "INICIADO")
        setResult(RESULT_OK, output);
        finish()
    }
}