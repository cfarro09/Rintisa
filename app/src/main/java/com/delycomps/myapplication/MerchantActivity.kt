package com.delycomps.myapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.delycomps.myapplication.Constants.RETURN_ACTIVITY
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.databinding.ActivityMerchantBinding
import com.delycomps.myapplication.model.DataMerchant
import com.delycomps.myapplication.model.PointSale
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

//            if (imageAfter == "") {
//                Snackbar.make(tabs, "La \"foto del despues\" es obligatoria.", Snackbar.LENGTH_LONG).setBackgroundTint(resources.getColor(
//                    R.color.colorSecondary
//                )).show()
//                return true
//            }
//            if (imageBefore == "") {
//                Snackbar.make(tabs, "La \"foto del antes\" es obligatoria.", Snackbar.LENGTH_LONG).setBackgroundTint(resources.getColor(
//                    R.color.colorSecondary
//                )).show()
//                return true
//            }
//            if (listMaterials.count() == 0) {
//                Snackbar.make(tabs, "Debe registrar al menos un material instalado", Snackbar.LENGTH_LONG).setBackgroundTint(resources.getColor(
//                    R.color.colorSecondary
//                )).show()
//                return true
//            }

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
                            "description_pricesurvey" to SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()),
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
                        merchantViewModel.closeMerchant(pointSale.visitId, imageBefore, imageAfter, Gson().toJson(listMaterialProcessed), Gson().toJson(listProductProcessed), listProductProcessed.count() > 0, SharedPrefsCache(this).getToken())
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
                val output = Intent()
                output.putExtra("refresh", "refresh")
                setResult(RESULT_OK, output);
                finish()
            } else {
                Toast.makeText(this, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show()
            }
        }

        binding = ActivityMerchantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pointSale = intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, if (pointSale.showSurvey) 3 else 2)
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