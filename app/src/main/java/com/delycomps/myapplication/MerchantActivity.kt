package com.delycomps.myapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.databinding.ActivityMerchantBinding
import com.delycomps.myapplication.model.DataMerchant
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.model.resImage
import com.delycomps.myapplication.ui.merchant.MerchantViewModel
import com.delycomps.myapplication.ui.merchant.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MerchantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantBinding
    private lateinit var merchantViewModel: MerchantViewModel
    private lateinit var pointSale: PointSale
    private lateinit var dialogLoading: AlertDialog
    private lateinit var tabs: TabLayout
    private var imageIndex: Int = 0

    private var listImages: MutableList<resImage> = arrayListOf()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId
        if (id == R.id.action_one) {
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialogLoading.show()
                        val imageBefore = merchantViewModel.urlBeforeImage.value ?: ""
                        val imageAfter = merchantViewModel.urlAfterImage.value ?: ""

                        listImages = arrayListOf()

                        if (imageBefore != "")
                            listImages.add(resImage("BEFORE", imageBefore, null))
                        if (imageAfter != "")
                            listImages.add(resImage("AFTER", imageAfter, null))

                        if (listImages.count() > 0) {
                            imageIndex = 0
                            merchantViewModel.uploadImage(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
                        } else {
                            closeSession()
                        }
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
        else if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun closeSession() {
        val imageBefore = merchantViewModel.urlBeforeImage.value ?: ""
        val imageAfter = merchantViewModel.urlAfterImage.value ?: ""
        val listMaterials = merchantViewModel.listMaterialSelected.value ?: emptyList()
        val listProducts = merchantViewModel.listProductSelected.value ?: emptyList()

        val dateCurrent = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        val listAvailabilitiesProcessed = (merchantViewModel.dataProducts.value ?: emptyList())
            .map { mapOf<String, Any>(
                "customerid" to pointSale.customerId,
                "description_availability" to dateCurrent,
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

        BDLocal(this).updatePointSaleManagement(pointSale.visitId, statusManagement, motive, observation)

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
            "description_pricesurvey" to dateCurrent,
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
            listProductProcessed.isNotEmpty(),
            Gson().toJson(listAvailabilitiesProcessed),
            (merchantViewModel.productsAvailability.value ?: emptyList()).isNotEmpty(),
            statusManagement, motive, observation, SharedPrefsCache(this).getToken(),
            null, pointSale.dateStart, pointSale.latitudeStart, pointSale.longitudeStart
        )
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
            val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val output = Intent()

            if (it) {
                Toast.makeText(this, "Se actualizó el punto de venta", Toast.LENGTH_LONG).show()
                BDLocal(this).deleteMerchantPricesFromVisit(pointSale.visitId)
                BDLocal(this).deleteMaterialFromVisit(pointSale.visitId)
                BDLocal(this).deleteProductAvailabilityFromVisit(pointSale.visitId)
                BDLocal(this).updatePointSaleLocal(pointSale.visitId, "VISITADO", "ENVIADO")

                listImages.forEach { r ->
                    try {
                        val res = File(r.path).delete()
                        Log.d("file-delete", "${r.path} -> $res")
                    } catch (e: Exception) {
                        Log.d("file-delete-error", "${r.path} -> error ${e.message.toString()}")
                    }
                }

                output.putExtra("statuslocal", "ENVIADO")
            } else {
                Toast.makeText(this, "Hay problema de conexión, cuando haya internet proceder a volver a guardar", Toast.LENGTH_LONG).show()
                output.putExtra("statuslocal", "NOENVIADO")
                BDLocal(this).updatePointSaleLocal(pointSale.visitId, "VISITADO", "NOENVIADO", null, null, dateString)
            }

            output.putExtra("status", "VISITADO")
            output.putExtra("datefinish", dateString)
            setResult(RESULT_OK, output)
            finish()
        }

        merchantViewModel.urlImage.observe(this) {
            if (it == "") {
                //error lo regresamos nomas
                dialogLoading.dismiss()
                val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                Toast.makeText(this, "Hay problema de conexión, cuando haya internet proceder a volver a guardar", Toast.LENGTH_LONG).show()
                BDLocal(this).updatePointSaleLocal(pointSale.visitId, "VISITADO", "NOENVIADO", null, null, dateString)
                val output = Intent()
                output.putExtra("statuslocal", "NOENVIADO")
                output.putExtra("status", "VISITADO")
                output.putExtra("datefinish", dateString)
                setResult(RESULT_OK, output)
                finish()
            } else {
                val im = listImages[imageIndex]
                merchantViewModel.uploadImageLocal(it, im.type)

                imageIndex += 1
                if (imageIndex == listImages.count()) {
                    closeSession()
                } else {
                    merchantViewModel.uploadImage(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
                }
            }
        }

        binding = ActivityMerchantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pointSale = intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        merchantViewModel.initialPriceProduct(BDLocal(this).getMerchantPrices(pointSale.visitId).toMutableList())

        merchantViewModel.initialProductAvailability(BDLocal(this).getProductsAvailability(pointSale.visitId).toMutableList())

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, pointSale.showSurvey, pointSale.showAvailability)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        tabs = binding.tabs
        tabs.setupWithViewPager(viewPager)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = pointSale.client
    }

    override fun onBackPressed() {
//        val output = Intent()
//        output.putExtra("status", "INICIADO")
//        setResult(RESULT_OK, output);
        finish()
    }
}