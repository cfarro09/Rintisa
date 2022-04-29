package com.delycomps.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.delycomps.myapplication.adapter.AdapterPointsale
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.DataSupervisor
import com.delycomps.myapplication.model.PointSale
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson


class SupervisorActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private lateinit var supervisorViewModel: SupervisorViewModel
    private var dialogFilter: AlertDialog? = null
    private lateinit var dialogLoading: AlertDialog
    private lateinit var rv: RecyclerView

    private var marketIdG: Int? = 0
    private lateinit var serviceG: String


    private var permissionGPS = false
    private var gpsEnabled = false
    private var lastLocation: Location? = null
    private var locationManager : LocationManager? = null
    private lateinit var mainViewModel: MainViewModel


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_supervisor, menu)
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
            R.id.action_assist -> {
                if (!permissionGPS || !gpsEnabled || lastLocation == null) {
                    if (!permissionGPS) {
                        Toast.makeText(this@SupervisorActivity, "Tiene que conceder permisos de ubicación", Toast.LENGTH_SHORT).show()
                        return true
                    }
                    if (!gpsEnabled) {
                        Toast.makeText(this@SupervisorActivity, "Tiene que activar su GPS", Toast.LENGTH_SHORT).show()
                        return true
                    }
                    if (lastLocation == null) {
                        Toast.makeText(this@SupervisorActivity, "Estamos mapeando su ubicación", Toast.LENGTH_SHORT).show()
                        return true
                    }
                }
                mainViewModel.saveAssistance(lastLocation!!.latitude, lastLocation!!.longitude, SharedPrefsCache(this).getToken())
            }
        }
        return false
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervisor)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        rv = findViewById(R.id.main_rv_point_sale)
        rv.layoutManager = LinearLayoutManager(this)

        supervisorViewModel = ViewModelProvider(this).get(SupervisorViewModel::class.java)

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
                permissionGPS = true
                mainViewModel.setGPSIsEnabled(true)
                Log.d("log_carlos", "ACCESS_FINE_LOCATION")
            } else if (permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                permissionGPS = true
                mainViewModel.setGPSIsEnabled(true)
                Log.d("log_carlos", "ACCESS_COARSE_LOCATION")
            }
        }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        mainViewModel.gpsEnabled.observe(this) {
            if (it == true) {
                try {
                    locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
                    gpsEnabled = true
                    Log.d("log_carlos", "CONNECTION UBICATION OK")
                } catch (e: java.lang.Exception) {
                    Log.d("log_carlos", "EXCEPTION AL CARGAR LA UBICACION")
                    permissionGPS = false
                }
            }
        }



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
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastLocation = location

            Toast.makeText(this@SupervisorActivity, "Ubicación actualizada: ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }
        override fun onProviderEnabled(provider: String) {
            gpsEnabled = true
            mainViewModel.setGPSIsEnabled(true)
            Log.d("log_carlos", "PROVIDER ENABLED")
            Snackbar.make(rv, "GPS activado", Snackbar.LENGTH_LONG).setBackgroundTint(resources.getColor(
                R.color.colorPrimary
            )).show()
        }
        override fun onProviderDisabled(provider: String) {
            Log.d("log_carlos", "PROVIDER DISABLED")
            gpsEnabled = false
            mainViewModel.setGPSIsEnabled(false)
            Snackbar.make(rv, "Debe activar su GPS", Snackbar.LENGTH_LONG).setBackgroundTint(resources.getColor(
                R.color.colorSecondary
            )).show()
        }
    }
    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        if (rv.adapter != null) {
            (rv.adapter as AdapterPointsale).setFilter(filter(supervisorViewModel.listPointSale.value ?: emptyList(), p0!!))
        }
        return false
    }
    private fun filter(models: List<PointSale>, query: String): List<PointSale> {

        val filteredModelList = ArrayList<PointSale>()
        for (model in models) {
            val text = model.client?.lowercase() + ""
            val user = model.user?.lowercase() + ""
            val code = model.clientCode?.lowercase() + ""

            if (text.contains(query.lowercase()) || user.contains(query.lowercase()) || code.contains(query.lowercase())) {
                filteredModelList.add(model)
            }
        }
        return filteredModelList
    }

    private fun manageDialogFilter(view: View) {
        val spinnerService = view.findViewById<Spinner>(R.id.spinner_service)
        val spinnerMarket = view.findViewById<AutoCompleteTextView>(R.id.spinner_market)
        val buttonSearch = view.findViewById<Button>(R.id.dialog_search)
        val buttonCancel = view.findViewById<Button>(R.id.dialog_cancel)

        spinnerService.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("MERCADERISMO", "IMPULSADOR"))

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, supervisorViewModel.dataMarket.value?.map { it.description } ?: emptyList())

        spinnerMarket.setAdapter(arrayAdapter)

        buttonCancel.setOnClickListener {
            dialogFilter?.dismiss()
        }
        buttonSearch.setOnClickListener {
            val service = spinnerService.selectedItem?.toString() ?: ""
            val market = spinnerMarket.text.toString()

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