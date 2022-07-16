package com.delycomps.rintisa

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.delycomps.rintisa.adapter.AdapterCustomer
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.model.Customer
import com.delycomps.rintisa.model.DataAuditor
import com.google.gson.Gson
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class AuditorActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var auditorViewModel: AuditorViewModel
    private var currentPhotoPath: String = ""
    private var dialogFilter: AlertDialog? = null
    private lateinit var dialogLoading: AlertDialog
    private lateinit var rv: RecyclerView

    private var marketIdG: Int? = 0
    private var positionG: Int? = 0


    private lateinit var mainViewModel: MainViewModel


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_auditor, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.queryHint = getString(R.string.search)
        super.onCreateOptionsMenu(menu)
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

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auditor)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        rv = findViewById(R.id.main_rv_point_sale)
        rv.layoutManager = LinearLayoutManager(this)

        auditorViewModel = ViewModelProvider(this).get(AuditorViewModel::class.java)

        val swiper: SwipeRefreshLayout = findViewById(R.id.main_swiper_refresh)
        swiper.setOnRefreshListener {
            if (marketIdG != null) {
                auditorViewModel.getCustomer(marketIdG!!, SharedPrefsCache(this).getToken())
                swiper.isRefreshing = false
            }
        }

        val jsonMerchant = SharedPrefsCache(this).get("data-auditor", "string")
        val dataAuditor = Gson().fromJson(jsonMerchant.toString(), DataAuditor::class.java)
        auditorViewModel.setMultiInitial(dataAuditor)

        val builderFilter: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogFilterView = inflater.inflate(R.layout.layout_auditor_filter, null)
        builderFilter.setView(dialogFilterView)
        dialogFilter = builderFilter.create()
        manageDialogFilter(dialogFilterView)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        auditorViewModel.loading.observe(this) {
            if (it)
                dialogLoading.show()
            else
                dialogLoading.dismiss()
        }
        auditorViewModel.error.observe(this) {
            if ((it ?: "") != "") {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        auditorViewModel.listCustomer.observe(this) {
            val a = this
            rv.adapter = AdapterCustomer(it, object : AdapterCustomer.ListAdapterListener {
                override fun onClickAtDetailCustomer(pointSale1: Customer, position: Int) {
                    if (pointSale1.status == "EN ESPERA") {
                        positionG = position
                        val intent = Intent(
                            rv.context,
                            AuditorDetailActivity::class.java
                        )
                        intent.putExtra(Constants.POINT_CUSTOMER, pointSale1)
                        startActivityForResult(intent, Constants.RETURN_ACTIVITY)
                    } else {
                        Toast.makeText(a, "El cliente ${pointSale1.client} ya fue gestionado", Toast.LENGTH_LONG).show()
                    }
                }
            }, true)
        }

        mainViewModel.resSaveAssistance.observe(this) {
            if (it.loading) {
                dialogLoading.show()
            } else {
                dialogLoading.dismiss()
            }
            if (!it.loading && it.success) {
                Toast.makeText(this, "Asistencia registrada", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        imageReturnedIntent: Intent?
    ){
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            Constants.RETURN_ACTIVITY -> {
                if ((imageReturnedIntent?.getStringExtra("status") ?: "") != "") {
                    (rv.adapter as AdapterCustomer).updateStatus(positionG ?: 0, imageReturnedIntent?.getStringExtra("status") ?: "")
                }
            }
        }
    }


    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        if (rv.adapter != null) {
            (rv.adapter as AdapterCustomer).setFilter(filter(auditorViewModel.listCustomer.value ?: emptyList(), p0!!))
        }
        return false
    }
    private fun filter(models: List<Customer>, query: String): List<Customer> {

        val filteredModelList = ArrayList<Customer>()
        for (model in models) {
            val text = model.client?.lowercase() + ""
            val code = model.clientCode?.lowercase() + ""

            if (text.contains(query.lowercase()) || code.contains(query.lowercase())) {
                filteredModelList.add(model)
            }
        }
        return filteredModelList
    }

    private fun manageDialogFilter(view: View) {

        val spinnerMarket = view.findViewById<AutoCompleteTextView>(R.id.spinner_market)

        val buttonSearch = view.findViewById<Button>(R.id.dialog_search)
        val buttonCancel = view.findViewById<Button>(R.id.dialog_cancel)

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, auditorViewModel.dataMarket.value?.map { it.description } ?: emptyList())

        spinnerMarket.setAdapter(arrayAdapter)

        buttonCancel.setOnClickListener {
            dialogFilter?.dismiss()
        }
        buttonSearch.setOnClickListener {
            val market = spinnerMarket.text.toString()

            if (market != "") {
                try {
                    val marketId = market.split(")")[0].replace("(", "").toDouble().toInt()
                    marketIdG = marketId
                    dialogFilter?.dismiss()
                    auditorViewModel.getCustomer(marketId, SharedPrefsCache(view.context).getToken())
                } catch (e: Exception) {
                    Toast.makeText(this, "No es un mercado valido", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}