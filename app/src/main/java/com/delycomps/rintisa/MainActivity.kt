package com.delycomps.rintisa

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.delycomps.rintisa.Constants.RETURN_ACTIVITY
import com.delycomps.rintisa.adapter.AdapterPointsale
import com.delycomps.rintisa.cache.BDLocal
import com.delycomps.rintisa.cache.Helpers
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.model.*
import com.delycomps.rintisa.ui.merchant.MerchantViewModel
import com.delycomps.rintisa.ui.promoter.PromoterViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val WRITE_EXTERNAL_STORAGE_PERMISSION = 10220
private const val CODE_RESULT_CAMERA_SELFIE = 10001
private const val CODE_RESULT_CAMERA_MERCHANT_VISITED = 10021
private const val TAG: String = "STRING_PRUEBAENTREGAS_YA"
private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var rv: RecyclerView
    private lateinit var mainViewModel: MainViewModel
    private lateinit var dialogLoading: AlertDialog
    private lateinit var dialogClose: AlertDialog
    private lateinit var dialogGeolocation: AlertDialog
    private lateinit var pointSale: PointSale
    private var permissionCamera = false
    private var permissionGPS = false
    private var gpsEnabled = false
    private var indexPosition = 0
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0
    private var lastLocation: Location? = null
    private var locationManager : LocationManager? = null
    private var currentPhotoPath: String = ""
    private lateinit var merchantViewModel: MerchantViewModel
    private lateinit var promoterViewModel: PromoterViewModel
    private var currentMerchantPhotoPath: String = ""
    private var typeImage: String = ""

    private var mainMenu: Menu? = null

    private var listImages: MutableList<resImage> = arrayListOf()
    private var imageIndex: Int = 0



    // The BroadcastReceiver used to listen from broadcasts from the service.
    private var myReceiver: MyReceiver? = null
    // A reference to the service used to get location updates.
    private var mService: LocationUpdatesService? = null
    // Tracks the bound state of the service.
    private var mBound = false
    // UI elements.
    private var mRequestLocationUpdatesButton: Button? = null
    private var mRemoveLocationUpdatesButton: Button? = null

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: LocationUpdatesService.LocalBinder = service as LocationUpdatesService.LocalBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val role = SharedPrefsCache(this).get("type", "string")

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_finish)?.isVisible = (role == "IMPULSADOR")

        mainMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_exit -> {
                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            locationManager?.removeUpdates(locationListener)
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
                return true
            }
            R.id.action_share_location -> {
                dialogGeolocation.show()
            }
            R.id.action_assist -> {
                if (!permissionGPS || !gpsEnabled || lastLocation == null) {
                    if (!permissionGPS) {
                        Toast.makeText(this@MainActivity, "Tiene que conceder permisos de ubicación", Toast.LENGTH_SHORT).show()
                        return true
                    }
                    if (!gpsEnabled) {
                        Toast.makeText(this@MainActivity, "Tiene que activar su GPS", Toast.LENGTH_SHORT).show()
                        return true
                    }
                    if (lastLocation == null) {
                        Toast.makeText(this@MainActivity, "Estamos mapeando su ubicación", Toast.LENGTH_SHORT).show()
                        return true
                    }
                }
                mainViewModel.saveAssistance(lastLocation!!.latitude, lastLocation!!.longitude, SharedPrefsCache(this).getToken())
            }
            R.id.action_finish -> {
                dialogClose.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myReceiver = MyReceiver()
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        promoterViewModel = ViewModelProvider(this).get(PromoterViewModel::class.java)
        merchantViewModel = ViewModelProvider(this).get(MerchantViewModel::class.java)

        val role = SharedPrefsCache(this).get("type", "string")

        mainMenu?.findItem(R.id.action_finish)?.isVisible = (role == "IMPULSADOR")

        //PERMISOS
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

        //END PERMISOS
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ))

        rv = findViewById(R.id.main_rv_point_sale)
        rv.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.remove_point_sale).setOnClickListener {
            BDLocal(this).deleteAllPointSale()
        }

        val swiper: SwipeRefreshLayout = findViewById(R.id.main_swiper_refresh)
        swiper.setOnRefreshListener {
            mainViewModel.getListLocation(this, SharedPrefsCache(this).getToken())
            swiper.isRefreshing = false
        }

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        mainViewModel.getListLocation(this, SharedPrefsCache(this).getToken())
        dialogLoading.show()

        mainViewModel.errorOnGetList.observe(this) {
            if (it != "" && it != null) {
                Toast.makeText(this, "Sin internet. Se cargaran los puntos de ventas guardados en memoria.", Toast.LENGTH_LONG).show()
            }
        }

        mainViewModel.resSaveAssistance.observe(this) {
            if (it.loading) {
                dialogLoading.show()
            } else {
                dialogLoading.dismiss()
            }
            if (!it.loading && it.success) {
                Toast.makeText(this, "Asistencia registrada", Toast.LENGTH_LONG).show()
                dialogClose.dismiss()
            }
        }

        val builderDialogCloseAssistance: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater1 = this.layoutInflater
        val dialogCloseAssistanceUI = inflater1.inflate(R.layout.layout_close_assistance, null)
        builderDialogCloseAssistance.setView(dialogCloseAssistanceUI)
        dialogClose = builderDialogCloseAssistance.create()
        manageDialogCloseAssistance(dialogCloseAssistanceUI, dialogClose)

        mainViewModel.listPointSale.observe(this) {
            dialogLoading.dismiss()
            rv.adapter = AdapterPointsale(it, object : AdapterPointsale.ListAdapterListener {
                override fun onClickAtDetailPointSale(pointSale1: PointSale, position: Int) {
//                    pointSale1.management = "EN ESPERA"
                    indexPosition = position
                    if (pointSale1.management == "VISITADO") {
                        if (!pointSale1.wasSaveOnBD) {
                            pointSale = pointSale1
                            dialogLoading.show()
                            val role = SharedPrefsCache(rv.context).get("type", "string")
                            if (role == "MERCADERISTA") {
                                closeMerchantValidate()
                            } else {
                                closePromoterValidate()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "El punto de venta ya fue gestionado", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }
                    else if (pointSale1.management == "INICIADO") {
                        val role = SharedPrefsCache(rv.context).get("type", "string")
                        val intent = Intent(
                            rv.context,
                            if (role == "IMPULSADOR") PromoterActivity::class.java else MerchantActivity::class.java
                        )
                        intent.putExtra(Constants.POINT_SALE_ITEM, pointSale1)
                        locationManager?.removeUpdates(locationListener)
                        startActivityForResult(intent, RETURN_ACTIVITY)
                    }
                    else
                    {
                        if (!permissionCamera || !permissionGPS || !gpsEnabled || lastLocation == null) {
                            if (!permissionCamera) {
                                Toast.makeText(this@MainActivity, "Tiene que conceder permisos de cámara", Toast.LENGTH_SHORT).show()
                                return
                            }
                            if (!permissionGPS) {
                                Toast.makeText(this@MainActivity, "Tiene que conceder permisos de ubicación", Toast.LENGTH_SHORT).show()
                                return
                            }
                            if (!gpsEnabled) {
                                Toast.makeText(this@MainActivity, "Tiene que activar su GPS", Toast.LENGTH_SHORT).show()
                                return
                            }
                            if (lastLocation != null) {
                                Toast.makeText(this@MainActivity, "Estamos mapeando su ubicación", Toast.LENGTH_SHORT).show()
                                return
                            }
                        } else {
                            pointSale = pointSale1
                            dialogLoading.show()
                            lastLatitude = lastLocation!!.latitude
                            lastLongitude = lastLocation!!.longitude
                            mainViewModel.initPointSale(SharedPrefsCache(rv.context).getToken(), pointSale1.visitId, "", lastLocation!!.latitude, lastLocation!!.longitude)
                        }
                    }
                }
            })
        }

        mainViewModel.sendInitPointSale.observe(this) {
            dialogLoading.dismiss()
            if (it != true) {
                val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                BDLocal(rv.context).updatePDVStartDate(pointSale.visitId, dateString, lastLatitude, lastLongitude)
                (rv.adapter as AdapterPointsale).updateDateStart(indexPosition, dateString, lastLatitude, lastLongitude)

                pointSale.dateStart = dateString
                pointSale.latitudeStart = lastLatitude
                pointSale.longitudeStart = lastLongitude
            } else {
                BDLocal(this).updatePointSaleLocal(pointSale.visitId, "INICIADO")
                (rv.adapter as AdapterPointsale).updateManagement(indexPosition, "INICIADO")
            }
            BDLocal(this).updatePointSaleLocal(pointSale.visitId, "INICIADO", "NOENVIADO", null, null, null)
            val role = SharedPrefsCache(rv.context).get("type", "string")
            val intent = Intent(
                rv.context,
                if (role == "IMPULSADOR") PromoterActivity::class.java else MerchantActivity::class.java
            )

            intent.putExtra(Constants.POINT_SALE_ITEM, pointSale)
            startActivityForResult(intent, RETURN_ACTIVITY)
        }

        supportActionBar?.title = SharedPrefsCache(rv.context).get("fullname", "string")?.toString() ?: pointSale.client

        merchantViewModel.urlImage.observe(this) {
            if (false) {
                Toast.makeText(this, "Hay problema de conexión, cuando haya internet proceder a volver a guardar - URL IMAGE", Toast.LENGTH_LONG).show()
                dialogLoading.dismiss()
            } else {
                val im = listImages[imageIndex]
                if (im.type == "AFTER") {
                    pointSale.imageAfter = it
                } else {
                    pointSale.imageBefore = it
                }
                imageIndex += 1
                if (imageIndex == listImages.count()) {
                    closeMerchant(pointSale)
                } else {
                    merchantViewModel.uploadImage(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
                }
            }
        }

        promoterViewModel.urlSelfie.observe(this) {
            if (it == "") {
                Toast.makeText(this, "Hay problema de conexión, cuando haya internet proceder a volver a guardar", Toast.LENGTH_LONG).show()
                dialogLoading.dismiss()
            } else {
                val im = listImages[imageIndex]
                promoterViewModel.updateProductImageLocal(im.type, it, this)

                imageIndex += 1
                if (imageIndex == listImages.count()) {
                    closePromoter()
                } else {
                    promoterViewModel.uploadSelfie(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
                }
            }
        }

        merchantViewModel.closingMerchant.observe(this) {
            dialogLoading.dismiss()
            if (it) {
                Toast.makeText(this, "Se actualizó el punto de venta", Toast.LENGTH_LONG).show()
                //limpiar información de materiales, encuesta de precios y de disponibilidad
                BDLocal(this).deleteMerchantPricesFromVisit(pointSale.visitId)
                BDLocal(this).deleteMaterialFromVisit(pointSale.visitId)
                BDLocal(this).deleteProductAvailabilityFromVisit(pointSale.visitId)
                //actualizar localmente el punto de venta
                BDLocal(this).updatePointSaleLocal(pointSale.visitId, "VISITADO", "ENVIADO")
                //actualizar el adaptador
                (rv.adapter as AdapterPointsale).updateManagement(indexPosition, "VISITADO", "", "ENVIADO")

                listImages.forEach { r ->
                    try {
                        val res = File(r.path).delete()
                        Log.d("file-delete", "${r.path} -> $res")
                    } catch (e: Exception) {
                        Log.d("file-delete-error", "${r.path} -> error ${e.message.toString()}")
                    }
                }
            } else {
                Toast.makeText(this, "Hay problema de conexión, cuando haya internet proceder a volver a guardar - CIERRE MERCHANT", Toast.LENGTH_LONG).show()
            }
        }

        promoterViewModel.closingPromoter.observe(this) {
            dialogLoading.dismiss()

            if (it) {
                Toast.makeText(this, "Se actualizó el punto de venta", Toast.LENGTH_LONG).show()
                BDLocal(this).deleteSalePromoterFromVisit(pointSale.visitId)
                BDLocal(this).deleteStockPromoterFromVisit(pointSale.visitId)
                BDLocal(this).updatePointSaleLocal(pointSale.visitId, "VISITADO", "ENVIADO")

                BDLocal(this).updatePointSaleLocal(pointSale.visitId, "VISITADO", "ENVIADO")
                //actualizar el adaptador
                (rv.adapter as AdapterPointsale).updateManagement(indexPosition, "VISITADO", "", "ENVIADO")

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
            }
        }

//        val builderDialogGeo: AlertDialog.Builder = AlertDialog.Builder(this)
//        val inflater2 = this.layoutInflater
//        val dialogGeoUI = inflater2.inflate(R.layout.layout_share_location, null)
//        builderDialogGeo.setView(dialogGeoUI)
//        dialogGeolocation = builderDialogGeo.create()
//
//        mRequestLocationUpdatesButton = dialogGeoUI.findViewById(R.id.request_location_updates_button)
//        mRemoveLocationUpdatesButton = dialogGeoUI.findViewById(R.id.remove_location_updates_button)
//
//        FirebaseApp.initializeApp(this)
    }

    private fun manageDialogCloseAssistance (view: View, dialog: AlertDialog) {
        val editQuantityTickets = view.findViewById<EditText>(R.id.quantity_day)
        val editCommentDay = view.findViewById<EditText>(R.id.comment_day)
        val buttonCancel = view.findViewById<Button>(R.id.dialog_cancel_assistance)
        val buttonSave = view.findViewById<Button>(R.id.dialog_save_close_assistance)

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonSave.setOnClickListener {
            val quantity = (if (editQuantityTickets.text.toString() == "") "0" else editQuantityTickets.text.toString()).toInt()
            val comment = editCommentDay.text.toString()

            mainViewModel.closeAssistance(quantity, comment, SharedPrefsCache(this).getToken())

        }
    }

    private fun closeMerchantValidate() {
        val listPoint = BDLocal(this).getPointSaleOne(pointSale.visitId)

        if (listPoint.isNotEmpty()) {
            val point = listPoint[0]

            val imageBefore = point.imageBeforeLocal ?: ""
            val imageAfter = point.imageAfterLocal ?: ""

            listImages = arrayListOf()

            if (imageBefore != "")
                listImages.add(resImage("BEFORE", imageBefore, null))
            if (imageAfter != "")
                listImages.add(resImage("AFTER", imageAfter, null))

            if (listImages.isNotEmpty()) {
                imageIndex = 0
                merchantViewModel.uploadImage(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
            } else {
                closeMerchant(point)
            }
        }
    }

    private fun closePromoterValidate() {
        val listProducts: List<SurveyProduct> = BDLocal(this).getSalePromoter(pointSale.visitId)

        listImages = listProducts.filter { (it.imageEvidenceLocal ?: "") != "" && it.imageEvidence == "" }.map { resImage(it.uuid.toString(), it.imageEvidenceLocal!!, null) }.toMutableList()

        if (listImages.isNotEmpty()) {
            imageIndex = 0
            promoterViewModel.uploadSelfie(File(listImages[imageIndex].path), SharedPrefsCache(this).getToken())
        } else {
            closePromoter()
        }
    }

    private fun closeMerchant (point: PointSale) {
        val listMaterials = BDLocal(this).getMaterialStock(pointSale.visitId).toMutableList()
        val listProducts = BDLocal(this).getMerchantPrices(pointSale.visitId).toMutableList()
        val listAvailability = BDLocal(this).getProductsAvailability(pointSale.visitId).toMutableList()

        val imageBefore = point.imageBefore ?: ""
        val imageAfter = point.imageAfter ?: ""

        val jsonMerchant = SharedPrefsCache(this).get("data-merchant", "string")
        val dataMerchant = Gson().fromJson(jsonMerchant.toString(), DataMerchant::class.java)

        val listAvailabilitiesProcessed = (dataMerchant.products)
            .map { mapOf(
                "customerid" to pointSale.customerId,
                "description_availability" to point.dateFinish,
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
                "flag_availabilitydetail" to (listAvailability).any { r -> r.productid == it.productId }
            ) }.toList()

        val statusManagement = merchantViewModel.management.value?.status_management ?: "EFECTIVA"
        val motive = merchantViewModel.management.value?.motive ?: ""
        val observation = merchantViewModel.management.value?.observation ?: ""

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
            "description_pricesurvey" to (point.dateFinish ?: ""),
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
            listAvailability.isNotEmpty(),
            statusManagement, motive, observation, SharedPrefsCache(this).getToken(),
            point.dateFinish, point.dateStart, point.latitudeStart, point.longitudeStart
        )
    }

    private fun closePromoter() {
        val listStocks: List<Stock> = BDLocal(this).getStockPromoter(pointSale.visitId)
        val listProducts: List<SurveyProduct> = BDLocal(this).getSalePromoter(pointSale.visitId)

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
            listProducts.isNotEmpty(),
            "",
            SharedPrefsCache(this).getToken(),
            pointSale.dateFinish,
            pointSale.dateStart, pointSale.latitudeStart, pointSale.longitudeStart
        )
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastLocation = location

            Toast.makeText(this@MainActivity, "Ubicación actualizada: ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
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
                    mainViewModel.uploadSelfie(f, SharedPrefsCache(this).getToken())
                } else {
                    dialogLoading.dismiss()
                    Toast.makeText(this, "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                }
            }
            RETURN_ACTIVITY -> {
                if ((imageReturnedIntent?.getStringExtra("status") ?: "") != "") {
                    (rv.adapter as AdapterPointsale).updateManagement(indexPosition, imageReturnedIntent?.getStringExtra("status") ?: "", imageReturnedIntent?.getStringExtra("datefinish") ?: "", imageReturnedIntent?.getStringExtra("statuslocal") ?: "")
                }
            }
            CODE_RESULT_CAMERA_MERCHANT_VISITED -> if (resultCode == RESULT_OK) {
                dialogLoading.show()
                val f = Helpers().saveBitmapToFile(File(currentMerchantPhotoPath))
                if (f != null) {
                    merchantViewModel.uploadWithBD(f, pointSale.visitId, typeImage, SharedPrefsCache(this).getToken())
                } else {
                    dialogLoading.dismiss()
                    Toast.makeText(this, "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
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

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        val builderDialogGeo: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater2 = this.layoutInflater
        val dialogGeoUI = inflater2.inflate(R.layout.layout_share_location, null)
        builderDialogGeo.setView(dialogGeoUI)
        dialogGeolocation = builderDialogGeo.create()

        mRequestLocationUpdatesButton = dialogGeoUI.findViewById(R.id.request_location_updates_button)
        mRemoveLocationUpdatesButton = dialogGeoUI.findViewById(R.id.remove_location_updates_button)

        mRequestLocationUpdatesButton!!.setOnClickListener(View.OnClickListener {
            if (permissionGPS && gpsEnabled) {
                mService!!.requestLocationUpdates()
            }
        })
        mRemoveLocationUpdatesButton!!.setOnClickListener(View.OnClickListener { mService!!.removeLocationUpdates() })


        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this))

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(
            Intent(this, LocationUpdatesService::class.java), mServiceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            myReceiver!!,
            IntentFilter(LocationUpdatesService.ACTION_BROADCAST)
        )
    }

    override fun onPause() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver!!)
        }catch (e: Exception) {

        }

        super.onPause()
    }

    override fun onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection)
            mBound = false
        }
        try {

            PreferenceManager.getDefaultSharedPreferences(applicationContext).unregisterOnSharedPreferenceChangeListener(this)
        }catch (e: Exception) {

        }
        super.onStop()
    }

    private class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location =
                intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
            if (location != null) {
                Toast.makeText(
                    context, Utils.getLocationText(location),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s == Utils.KEY_REQUESTING_LOCATION_UPDATES) {
            setButtonsState(
                sharedPreferences.getBoolean(
                    Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false
                )
            )
        }
    }

    private fun setButtonsState(requestingLocationUpdates: Boolean) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton?.isEnabled = false
            mRemoveLocationUpdatesButton?.isEnabled = true
        } else {
            mRequestLocationUpdatesButton?.isEnabled = true
            mRemoveLocationUpdatesButton?.isEnabled = false
        }
    }
}