package com.delycomps.rintisa

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.delycomps.rintisa.adapter.AdapterPointsale
import com.delycomps.rintisa.adapter.AdapterVisitPending
import com.delycomps.rintisa.cache.BDLocal
import com.delycomps.rintisa.cache.Helpers
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val CODE_RESULT_CAMERA_SELFIE = 10001
private const val WRITE_EXTERNAL_STORAGE_PERMISSION = 10220

class SupervisorActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var supervisorViewModel: SupervisorViewModel
    private var currentPhotoPath: String = ""
    private var dialogFilter: AlertDialog? = null
    private var dialogPending: AlertDialog? = null
    private lateinit var dialogLoading: AlertDialog
    private lateinit var rv: RecyclerView
    private lateinit var rvPending: RecyclerView
    private var service: String = "MERCADERISMO"
    private var marketIdG: Int? = 0
    private lateinit var serviceG: String
    private var todayG: Boolean = false
    private var permissionCamera = false
    private lateinit var pointSale: PointSale
    private lateinit var visitPending: VisitSupervisor
    private var permissionGPS = false
    private var gpsEnabled = false
    private var lastLocation: Location? = null
    private var locationManager : LocationManager? = null
    private lateinit var mainViewModel: MainViewModel
    private var indexPosition = 0
    private var typeUpload = ""
    private var listImages: MutableList<resImage> = arrayListOf()
    private var imageIndex: Int = 0

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
            R.id.action_pending -> {
                val visits = BDLocal(this).getListVisitSupervisor()
                val context = this
                rvPending.adapter = AdapterVisitPending(visits, object : AdapterVisitPending.ListAdapterListener {
                    override fun onClickAtDetailVisitSupervisor(ps: VisitSupervisor, position: Int) {
                        visitPending = ps
                        dialogLoading.show()
                        if (ps.createDate.isNullOrEmpty()) {
                            sendVisitPending()
                        } else {
                            listImages = arrayListOf()
                            if (ps.type == "MERCADERISMO") {
                                imageIndex = 0
                                listImages.add(resImage("image1", ps.image1 ?: "", null))
                                typeUpload = "cacheo"
                                mainViewModel.uploadSelfie(File(listImages[imageIndex].path), SharedPrefsCache(context).getToken())
                            }
                        }
                    }
                })
                dialogPending?.show()
            }
        }
        return false
    }

    fun sendVisitPending() {
        val ob = JSONObject()
        ob.put("auditdetail", visitPending.auditjson)
        ob.put("customerid", visitPending.customerId)
        ob.put("image1", visitPending.image1)
        ob.put("initdate", visitPending.createDate)
        ob.put("latitude", visitPending.latitude)
        ob.put("longitude", visitPending.longitude)
        ob.put("aux_userid", visitPending.userId)
        ob.put("type", visitPending.type)
        ob.put("visitid", visitPending.visitId)
        ob.put("comment", visitPending.comment)
        supervisorViewModel.executeSupervisor(ob, "UFN_MANAGE_SUPERVISOR_MERCHANT_OFF", SharedPrefsCache(this).getToken())
    }

    @SuppressLint("MissingPermission")
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
            if (permissions.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false)) {
                permissionCamera = true
                Log.d("log_carlos", "WRITE_EXTERNAL_STORAGE")
            }
        }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ))

        mainViewModel.gpsEnabled.observe(this) {
            if (it == true) {
                try {
                    locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
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
                supervisorViewModel.getListLocation(marketIdG!!, serviceG, todayG, SharedPrefsCache(this).getToken())
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

        val builderPending: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater1 = this.layoutInflater
        val dialogPendingView = inflater1.inflate(R.layout.layout_supervisor_pending, null)
        builderPending.setView(dialogPendingView)
        dialogPending = builderPending.create()
        rvPending = dialogPendingView.findViewById(R.id.supervisor_rv_pending)
        rvPending.layoutManager = LinearLayoutManager(this)

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

        mainViewModel.urlSelfie.observe(this) {
            dialogLoading.dismiss()
            val userid = SharedPrefsCache(this).get("userid", "string").toString().toInt()
            if (typeUpload == "cacheo") {
                if (it == "") {
                    Toast.makeText(this, "Tiene problemas de red, cuando tenga internet por favor volver a intentar", Toast.LENGTH_LONG).show()
                } else {
                    when (listImages[imageIndex].type) {
                        "image1" -> visitPending.image1 = it
                        "image2" -> visitPending.image2 = it
                        "image3" -> visitPending.image3 = it
                        "image4" -> visitPending.image4 = it
                        "image5" -> visitPending.image5 = it
                    }
                    imageIndex++
                    if (imageIndex == listImages.count()) {
                        sendVisitPending()
                    } else {
                        typeUpload = "cacheo"
                        mainViewModel.uploadSelfie(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
                    }
                }
            } else {
                if (it == "") {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val fullDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                    val visitSup = VisitSupervisor(pointSale.customerId, pointSale.userid ?: 0, userid, date, fullDate, currentPhotoPath, service, lastLocation?.latitude ?: 0.00, lastLocation?.longitude ?: 0.00)
                    visitSup.customer = pointSale.client
                    visitSup.visitId = pointSale.visitId
                    BDLocal(this).addVisitSupervisor(visitSup)
                    Toast.makeText(this, "Tiene problemas de red, cuando tenga internet por favor proceder a enviar la visita", Toast.LENGTH_LONG).show()
                } else {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val visitSup = VisitSupervisor(pointSale.customerId, pointSale.userid ?: 0, userid, date, "", "", service, 0.00, 0.00)
                    visitSup.customer = pointSale.client
                    visitSup.visitId = pointSale.visitId
                    BDLocal(this).addVisitSupervisor(visitSup)
                }

                (rv.adapter as AdapterPointsale).updateManagementSup(indexPosition, "INICIADO")

                if (service == "MERCADERISMO") {
                    val intent = Intent(
                        rv.context,
                        MerchantSupervisorActivity::class.java
                    )
                    intent.putExtra(Constants.POINT_SALE_ITEM, pointSale)
                    intent.putExtra(Constants.POINT_SALE_SERVICE, service)
                    startActivityForResult(intent, Constants.RETURN_ACTIVITY)
                } else {
                    val intent = Intent(
                        rv.context,
                        PromoterSupervisorActivity::class.java
                    )
                    intent.putExtra(Constants.POINT_SALE_ITEM, pointSale)
                    intent.putExtra(Constants.POINT_SALE_SERVICE, service)
                    startActivityForResult(intent, Constants.RETURN_ACTIVITY)
                }
            }
        }

        supervisorViewModel.listPointSale.observe(this) {
            rv.adapter = AdapterPointsale(it, object : AdapterPointsale.ListAdapterListener {
                override fun onClickAtDetailPointSale(pointSale1: PointSale, position: Int) {
                    indexPosition = position
                    if (!permissionCamera || !permissionGPS || !gpsEnabled || lastLocation == null) {
                        if (!permissionCamera) {
                            Toast.makeText(this@SupervisorActivity, "Tiene que conceder permisos de cámara", Toast.LENGTH_SHORT).show()
                            return
                        }
                        if (!permissionGPS) {
                            Toast.makeText(this@SupervisorActivity, "Tiene que conceder permisos de ubicación", Toast.LENGTH_SHORT).show()
                            return
                        }
                        if (!gpsEnabled) {
                            Toast.makeText(this@SupervisorActivity, "Tiene que activar su GPS", Toast.LENGTH_SHORT).show()
                            return
                        }
                        if (lastLocation != null) {
                            Toast.makeText(this@SupervisorActivity, "Estamos mapeando su ubicación", Toast.LENGTH_SHORT).show()
                            return
                        }
                    } else {
                        pointSale = pointSale1
                        dispatchTakePictureIntent()
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

        supervisorViewModel.resExecute.observe(this) {
            if ((it.result) == "UFN_MANAGE_SUPERVISOR_MERCHANT_OFF") {
                if (!it.loading && it.success) {
                    BDLocal(this).deleteSupervisorVisit(visitPending.uuid!!)
                    dialogLoading.dismiss()
                    dialogPending?.dismiss()
                    Toast.makeText(this, "Se envió correctamente", Toast.LENGTH_LONG).show()
                    supervisorViewModel.initExecute()
                } else if (!it.loading && !it.success) {
                    dialogLoading.dismiss()
                    Toast.makeText(this, "Hay problema de conexión, cuando haya internet proceder a volver a guardar", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun dispatchTakePictureIntent(code: Int = CODE_RESULT_CAMERA_SELFIE) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager).also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(code)
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.delycomps.rintisa.provider",
                        photoFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, code)
                }
            }
        }
    }

    private fun createImageFile(code: Int): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastLocation = location
            Toast.makeText(this@SupervisorActivity, "Ubicación actualizada: ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) { }
        override fun onProviderEnabled(provider: String) {
            gpsEnabled = true
            mainViewModel.setGPSIsEnabled(true)
            Snackbar.make(rv, "GPS activado", Snackbar.LENGTH_LONG).setBackgroundTint(resources.getColor(
                R.color.colorPrimary
            )).show()
        }
        override fun onProviderDisabled(provider: String) {
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        imageReturnedIntent: Intent?
    ){
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            CODE_RESULT_CAMERA_SELFIE -> if (resultCode == RESULT_OK) {
                dialogLoading.show()
                val f = Helpers().saveBitmapToFile(File(currentPhotoPath))
                if (f != null) {
                    val jsonRb = Gson().toJson(
                        RequestBodyX("UFN_MANAGE_SUPERVISOR_PROMOTER_INS1", "UFN_MANAGE_SUPERVISOR_PROMOTER_INS1", mapOf<String, Any>(
                            "customerid" to pointSale.customerId,
                            "aux_userid" to (pointSale.userid ?: 0),
                            "service" to service,
                            "latitude" to (lastLocation?.latitude ?: 0.00),
                            "longitude" to (lastLocation?.longitude ?: 0.00),
                        ))
                    )
                    typeUpload = "selfie"
                    mainViewModel.uploadSelfie(f, SharedPrefsCache(this).getToken(), jsonRb)
                } else {
                    dialogLoading.dismiss()
                    Toast.makeText(this, "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                }
            }
            Constants.RETURN_ACTIVITY -> {
                if ((imageReturnedIntent?.getStringExtra("status") ?: "") != "") {
                    (rv.adapter as AdapterPointsale).updateManagementSup(indexPosition, imageReturnedIntent?.getStringExtra("status") ?: "")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_PERMISSION ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionCamera = true
                } else {
                    Toast.makeText(this, "Por favor considere en dar permisos de cámara.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun manageDialogFilter(view: View) {
        val spinnerService = view.findViewById<Spinner>(R.id.spinner_service)
        val spinnerMarket = view.findViewById<AutoCompleteTextView>(R.id.spinner_market)
        val switchDayVisit = view.findViewById<SwitchMaterial>(R.id.switch_day_visit)
        val buttonSearch = view.findViewById<Button>(R.id.dialog_search)
        val buttonCancel = view.findViewById<Button>(R.id.dialog_cancel)

        spinnerService.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("MERCADERISMO", "IMPULSADOR"))

        spinnerService.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val valueSelected = spinnerService.selectedItem.toString()
                service = valueSelected
            }
        }

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
                todayG = switchDayVisit.isChecked
                dialogFilter?.dismiss()
                supervisorViewModel.getListLocation(marketId, service, switchDayVisit.isChecked, SharedPrefsCache(view.context).getToken())
            }
        }
    }
}