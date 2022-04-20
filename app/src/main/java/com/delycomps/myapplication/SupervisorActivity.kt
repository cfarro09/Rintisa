package com.delycomps.myapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.delycomps.myapplication.adapter.AdapterPointsale
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.DataMerchant
import com.delycomps.myapplication.model.DataSupervisor
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.ui.merchant.MerchantViewModel
import com.google.gson.Gson

class SupervisorActivity : AppCompatActivity() {

    private lateinit var supervisorViewModel: SupervisorViewModel
    private var dialogFilter: AlertDialog? = null
    private lateinit var dialogLoading: AlertDialog
    private lateinit var rv: RecyclerView

    private var marketIdG: Int? = 0
    private lateinit var serviceG: String

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_supervisor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter ->
                dialogFilter?.show()
            R.id.action_exit -> {
                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            SharedPrefsCache(this).removeToken()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    }
                }
                val builder = AlertDialog.Builder(this)
                builder.setMessage("¿Está seguro de cerrar su sesión?")
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
        setContentView(R.layout.activity_supervisor)

        rv = findViewById(R.id.main_rv_point_sale)
        rv.layoutManager = LinearLayoutManager(this)

        supervisorViewModel = ViewModelProvider(this).get(SupervisorViewModel::class.java)

        val swiper: SwipeRefreshLayout = findViewById(R.id.main_swiper_refresh)
        swiper.setOnRefreshListener {
            if (marketIdG != null) {
                supervisorViewModel.getListLocation(marketIdG!!, serviceG, SharedPrefsCache(this).getToken())
                swiper.isRefreshing = false
            }
        }

        val jsonMerchant = SharedPrefsCache(this).get("data-supervisor", "string")
        val dataSupervisor = Gson().fromJson(jsonMerchant.toString(), DataSupervisor::class.java)
        supervisorViewModel.setMultiInitial(dataSupervisor)

        val builderFilter: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogFilterView = inflater.inflate(R.layout.layout_supervisor_filter, null)
        builderFilter.setView(dialogFilterView)
        dialogFilter = builderFilter.create()
        manageDialogFilter(dialogFilterView)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        supervisorViewModel.loading.observe(this) {
            if (it)
                dialogLoading.show()
            else
                dialogLoading.dismiss()
        }
        supervisorViewModel.error.observe(this) {
            if ((it ?: "") != "") {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        supervisorViewModel.listPointSale.observe(this) {
            rv.adapter = AdapterPointsale(it, object : AdapterPointsale.ListAdapterListener {
                override fun onClickAtDetailPointSale(pointSale1: PointSale, position: Int) {
                    val intent = Intent(
                        rv.context,
                        MerchantSupervisorActivity::class.java
                    )
                    intent.putExtra(Constants.POINT_SALE_ITEM, pointSale1)
                    startActivityForResult(intent, Constants.RETURN_ACTIVITY)
                }
            }, true)
        }
    }

    private fun manageDialogFilter(view: View) {
        val spinnerService = view.findViewById<Spinner>(R.id.spinner_service)
        val spinnerMarket = view.findViewById<Spinner>(R.id.spinner_market)
        val buttonSearch = view.findViewById<Button>(R.id.dialog_search)
        val buttonCancel = view.findViewById<Button>(R.id.dialog_cancel)

        spinnerService.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("MERCADERISMO", "IMPULSADOR"))
        spinnerMarket.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, supervisorViewModel.dataMarket.value?.map { it.description } ?: emptyList())

        buttonCancel.setOnClickListener {
            dialogFilter?.dismiss()
        }
        buttonSearch.setOnClickListener {
            val service = spinnerService.selectedItem?.toString() ?: ""
            val market = spinnerMarket.selectedItem?.toString() ?: ""

            if (service != "" && market != "") {
                val marketId = market.split(")")[0].replace("(", "").toDouble().toInt()
                marketIdG = marketId
                serviceG = service
                dialogFilter?.dismiss()
                supervisorViewModel.getListLocation(marketId, service, SharedPrefsCache(view.context).getToken())
            }
        }
    }
}