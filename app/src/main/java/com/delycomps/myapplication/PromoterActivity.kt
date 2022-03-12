package com.delycomps.myapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.ui.promoter.SectionsPagerAdapter
import com.delycomps.myapplication.databinding.ActivityPromoterBinding
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.ui.promoter.PromoterViewModel
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class PromoterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPromoterBinding
    private lateinit var tabs: TabLayout
    private lateinit var promoterViewModel: PromoterViewModel
    private lateinit var dialogLoading: AlertDialog
    private lateinit var pointSale: PointSale

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId

        if (id == R.id.action_one) {
            val listStocks = promoterViewModel.listStockSelected.value ?: emptyList()
            val listProducts = promoterViewModel.listProductSelected.value ?: emptyList()
            val listMerchandise = promoterViewModel.dataMerchandise.value ?: emptyList()

            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialogLoading.show()
                        val listStockProcessed = listStocks.map { mapOf<String, Any>(
                            "product" to it.product.toString(),
                            "brand" to it.brand.toString(),
                            "category" to it.type.toString()
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
                            "price" to 0,
                            "merchant" to (it.merchant ?: ""),
                            "url_evidence" to (it.imageEvidence ?: ""),
                            "total_detail" to 0,
                            "description_detail" to (it.description ?: ""),
                            "status_detail" to "ACTIVO",
                            "type_detail" to "NINGUNO",
                            "operation" to "INSERT",
                        ) }.toList()

                        val merchandises = Gson().toJson(listMerchandise.filter { it.flag == true }.joinToString { it.description }).replace("\"", "")
                        Log.d("log_carlos_listStocks", Gson().toJson(listStockProcessed))
                        Log.d("log_carlos_listProducts", Gson().toJson(listProducts))
                        Log.d("log_carlos_listMerchandise", merchandises)

                        promoterViewModel.closePromoter(
                            pointSale.visitId,
                            Gson().toJson(listStockProcessed),
                            Gson().toJson(listProductProcessed),
                            listProducts.count() > 0,
                            merchandises,
                            SharedPrefsCache(this).getToken()
                        )
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
        promoterViewModel = ViewModelProvider(this).get(PromoterViewModel::class.java)

        binding = ActivityPromoterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        promoterViewModel.getMainMulti(SharedPrefsCache(this).getToken())

        promoterViewModel.loadingInital.observe(this) {
            if (it == true) {
                dialogLoading.show()
            } else if (it == false){
                dialogLoading.dismiss()
            }
        }

        promoterViewModel.closingPromoter.observe(this) {
            dialogLoading.dismiss()
            if (it) {
                Toast.makeText(this, "Se actualizó el punto de venta", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show()
            }
        }
        pointSale = intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.promoterViewPager
        viewPager.adapter = sectionsPagerAdapter
        tabs = binding.promoterTabs
        tabs.setupWithViewPager(viewPager)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = pointSale.client
    }
}