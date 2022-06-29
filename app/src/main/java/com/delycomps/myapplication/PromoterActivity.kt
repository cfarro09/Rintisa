package com.delycomps.myapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.ui.promoter.SectionsPagerAdapter
import com.delycomps.myapplication.databinding.ActivityPromoterBinding
import com.delycomps.myapplication.model.*
import com.delycomps.myapplication.ui.promoter.PromoterViewModel
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PromoterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPromoterBinding
    private lateinit var tabs: TabLayout
    private lateinit var promoterViewModel: PromoterViewModel
    private lateinit var dialogLoading: AlertDialog
    private lateinit var pointSale: PointSale

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
                        val listProducts = promoterViewModel.listProductSelected.value ?: emptyList()
                        listImages = listProducts.filter { (it.imageEvidenceLocal ?: "") != "" && it.imageEvidence == "" }.map { resImage(it.uuid.toString(), it.imageEvidenceLocal!!, null) }.toMutableList()

                        if (listImages.count() > 0) {
                            imageIndex = 0
                            promoterViewModel.uploadSelfie(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
                        } else {
                            closePromoter()
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
        } else if (item.itemId == android.R.id.home) {
            val output = Intent()
            output.putExtra("status", "INICIADO")
            BDLocal(this).updatePointSaleLocal(pointSale.visitId, "INICIADO", "NOENVIADO", null, null, null)
            setResult(RESULT_OK, output)
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun closePromoter() {
        val listStocks = promoterViewModel.listStockSelected.value ?: emptyList()
        val listProducts = promoterViewModel.listProductSelected.value ?: emptyList()

        val listStockProcessed = listStocks.map { mapOf<String, Any>(
            "product" to it.product.toString(),
            "brand" to it.brand.toString(),
            "type" to it.type.toString()
        ) }.toList()

        val listProductProcessed = listProducts.map { mapOf<String, Any>(
            "subtotal" to 0,
            "total" to 0,
            "description_sale" to SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(
                Date()
            ),
            "status_sale" to "ACTIVO",
            "type_sale" to "NINGUNO",
            "productid" to it.productId,
            "quantity" to it.quantity,
            "measure_unit" to (it.measureUnit ?: ""),
            "price" to 0,
            "merchant" to (it.merchant ?: ""),
            "url_evidence" to (it.imageEvidence ?: ""),
            "total_detail" to 0,
            "description_detail" to (it.description ?: ""),
            "status_detail" to "ACTIVO",
            "type_detail" to "NINGUNO",
            "operation" to "INSERT",
        ) }.toList()

        promoterViewModel.closePromoter(
            pointSale.visitId,
            Gson().toJson(listStockProcessed),
            Gson().toJson(listProductProcessed),
            listProducts.count() > 0,
            "",
            SharedPrefsCache(this).getToken())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        promoterViewModel = ViewModelProvider(this).get(PromoterViewModel::class.java)

        binding = ActivityPromoterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pointSale = intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        val jsonPromoter = SharedPrefsCache(this).get("data-promoter", "string")
        val dataPromoter = Gson().fromJson(jsonPromoter.toString(), DataPromoter::class.java)
        promoterViewModel.setMultiInitial(dataPromoter)

        val listStock: List<Stock> = BDLocal(this).getStockPromoter(pointSale.visitId)
        val listProducts: List<SurveyProduct> = BDLocal(this).getSalePromoter(pointSale.visitId)
        promoterViewModel.initialDataPromoter(listStock, listProducts)

        promoterViewModel.urlSelfie.observe(this) {
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
                promoterViewModel.updateProductImage(im.type, it, this)

                imageIndex += 1
                if (imageIndex == listImages.count()) {
                    closePromoter()
                } else {
                    promoterViewModel.uploadSelfie(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
                }
            }
        }

        promoterViewModel.closingPromoter.observe(this) {
            dialogLoading.dismiss()
            val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val output = Intent()

            if (it) {
                Toast.makeText(this, "Se actualizó el punto de venta", Toast.LENGTH_LONG).show()
                BDLocal(this).deleteSalePromoterFromVisit(pointSale.visitId)
                BDLocal(this).deleteStockPromoterFromVisit(pointSale.visitId)
                BDLocal(this).updatePointSaleLocal(pointSale.visitId, "VISITADO", "ENVIADO")

                output.putExtra("status", "VISITADO")
                output.putExtra("statuslocal", "ENVIADO")

                listImages.forEach { r ->
                    try {
                        val res = File(r.path).delete()
                        Log.d("file-delete", "${r.path} -> $res")
                    } catch (e: Exception) {
                        Log.d("file-delete-error", "${r.path} -> error ${e.message.toString()}")
                    }
                }
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


        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.promoterViewPager
        viewPager.adapter = sectionsPagerAdapter
        tabs = binding.promoterTabs
        tabs.setupWithViewPager(viewPager)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = pointSale.client
    }
    override fun onBackPressed() {
        val output = Intent()
        BDLocal(this).updatePointSaleLocal(pointSale.visitId, "INICIADO", "NOENVIADO", null, null, null)
        output.putExtra("status", "INICIADO")
        setResult(RESULT_OK, output)
        finish()
//        super.onBackPressed()
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        return super.onKeyDown(keyCode, event)
//        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//            val output = Intent()
//            output.putExtra("status", "INICIADO")
//            setResult(RESULT_OK, output);
//        }
//    }

}