package com.delycomps.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.delycomps.myapplication.adapter.AdapterPointsale
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.ui.merchant.MerchantViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val WRITE_EXTERNAL_STORAGE_PERMISSION = 10220
private const val CODE_RESULT_CAMERA_SELFIE = 10001
private const val CODE_RESULT_CAMERA_MERCHANT_VISITED = 10021
private const val CODE_RESULT_GALLERY = 10002

class MainActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private lateinit var mainViewModel: MainViewModel
    private lateinit var dialogLoading: AlertDialog
    private lateinit var pointSale: PointSale
    private var permissionCamera = false
    private var permissionGPS = false
    private var gpsEnabled = false
    private var indexPosition = 0
    private var lastLocation: Location? = null
    private var locationManager : LocationManager? = null
    private var currentPhotoPath: String = ""
    private lateinit var merchantViewModel: MerchantViewModel
    private var currentMerchantPhotoPath: String = ""
    private lateinit var currentMerchantPhotoUri: Uri
    private var fromImage: String = ""
    private var typeImage: String = ""

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_exit) {
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
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        merchantViewModel = ViewModelProvider(this).get(MerchantViewModel::class.java)

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
                    locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
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

        val swiper: SwipeRefreshLayout = findViewById(R.id.main_swiper_refresh)
        swiper.setOnRefreshListener {
            mainViewModel.getListLocation(SharedPrefsCache(this).getToken())
            swiper.isRefreshing = false
        }

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        mainViewModel.getListLocation(SharedPrefsCache(this).getToken())
        dialogLoading.show()

        mainViewModel.errorOnGetList.observe(this) {
            if (it != "" && it != null) {
                Toast.makeText(this, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show()
            }
        }

        val builderDialogMerchantVisited: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogMerchantVisitedUI = inflater.inflate(R.layout.layout_merchant_visited, null)
        builderDialogMerchantVisited.setView(dialogMerchantVisitedUI)
        val dialogMerchantVisited = builderDialogMerchantVisited.create()
        manageDialogMerchantVisited(dialogMerchantVisitedUI, dialogMerchantVisited)

        mainViewModel.listPointSale.observe(this) {
            dialogLoading.dismiss()
            rv.adapter = AdapterPointsale(it, object : AdapterPointsale.ListAdapterListener {
                override fun onClickAtDetailPointSale(pointSale1: PointSale, position: Int) {
//                    pointSale1.management = "INICIADO"
                    if (pointSale1.management == "VISITADO")
                    {
                        val role = SharedPrefsCache(rv.context).get("type", "string")
                        if (role == "MERCADERISTA") {
                            indexPosition = position
                            pointSale = pointSale1
                            dialogMerchantVisitedUI.findViewById<TextView>(R.id.dialog_client_name).text = pointSale1.client
                            dialogMerchantVisited.show()

                            if (pointSale1.imageBefore != null && pointSale1.imageBefore != "") {
                                Glide.with(rv.context)
                                    .load(pointSale1.imageBefore)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .placeholder(R.drawable.loadingtmp)
                                    .into(dialogMerchantVisitedUI.findViewById(R.id.view_image_before))
                            } else {
                                dialogMerchantVisitedUI.findViewById<ImageView>(R.id.view_image_before).setImageDrawable(null)
                            }


                            if (pointSale1.imageAfter != null && pointSale1.imageAfter != "") {
                                Glide.with(rv.context)
                                    .load(pointSale1.imageAfter)
                                    .placeholder(R.drawable.loadingtmp)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .into(dialogMerchantVisitedUI.findViewById(R.id.view_image_after))
                            } else {
                                dialogMerchantVisitedUI.findViewById<ImageView>(R.id.view_image_after).setImageDrawable(null)
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "El punto de venta ya fue gestionado", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }
                    else if (pointSale1.management == "INICIADO")
                    {
                        val role = SharedPrefsCache(rv.context).get("type", "string")
                        val intent = Intent(
                            rv.context,
                            if (role == "IMPULSADOR") PromoterActivity::class.java else MerchantActivity::class.java
                        )
                        intent.putExtra(Constants.POINT_SALE_ITEM, pointSale1)
                        locationManager?.removeUpdates(locationListener)
                        rv.context.startActivity(intent)
                    }
                    else
                    {
                        if (!permissionCamera || !permissionGPS || !gpsEnabled) {
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
                            dispatchTakePictureIntent()
                        }
                    }
                }
            })
        }

        merchantViewModel.urlImageWithBD.observe(this) {
            if (!it.loading) {
                dialogLoading.dismiss()
                if (it.success) {
                    if (typeImage == "AFTER") {
                        (rv.adapter as AdapterPointsale).updateImageAfter(indexPosition, it.result)
                        if (fromImage == "CAMERA") {
                            dialogMerchantVisitedUI.findViewById<ImageView>(R.id.view_image_after).setImageBitmap(BitmapFactory.decodeFile(currentMerchantPhotoPath))
                        } else {
                            dialogMerchantVisitedUI.findViewById<ImageView>(R.id.view_image_after).setImageURI(currentMerchantPhotoUri)
                        }
                    } else {
                        (rv.adapter as AdapterPointsale).updateImageBefore(indexPosition, it.result)
                        if (fromImage == "CAMERA") {
                            dialogMerchantVisitedUI.findViewById<ImageView>(R.id.view_image_before).setImageBitmap(BitmapFactory.decodeFile(currentMerchantPhotoPath))
                        } else {
                            dialogMerchantVisitedUI.findViewById<ImageView>(R.id.view_image_before).setImageURI(currentMerchantPhotoUri)
                        }
                    }
                    Toast.makeText(this@MainActivity, "Se subió la imagen con exito!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, Constants.ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            }
        }

        mainViewModel.loadingSelfie.observe(this) {
            if ((mainViewModel.urlSelfie.value ?: "") == "") {
                dialogLoading.dismiss()
                Toast.makeText(rv.context, Constants.ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
            } else {
                mainViewModel.initPointSale(SharedPrefsCache(this).getToken(), pointSale.visitId, mainViewModel.urlSelfie.value ?: "", lastLocation!!.latitude, lastLocation!!.longitude)
            }
        }

        mainViewModel.sendInitPointSale.observe(this) {
            dialogLoading.dismiss()
            if (it == true) {
                val role = SharedPrefsCache(rv.context).get("type", "string")
                val intent = Intent(
                    rv.context,
                    if (role == "IMPULSADOR") PromoterActivity::class.java else MerchantActivity::class.java
                )
                intent.putExtra(Constants.POINT_SALE_ITEM, pointSale)
                locationManager?.removeUpdates(locationListener)
                rv.context.startActivity(intent)
            } else {
                Toast.makeText(this@MainActivity, Constants.ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
            }
        }

        supportActionBar?.title = SharedPrefsCache(rv.context).get("fullname", "string")?.toString() ?: pointSale.client
    }

    private fun manageDialogMerchantVisited (view: View, dialog: AlertDialog) {
        view.findViewById<ImageButton>(R.id.image_before).setOnClickListener {
            typeImage = "BEFORE"
            fromImage = "CAMERA"
            dispatchTakePictureIntent(CODE_RESULT_CAMERA_MERCHANT_VISITED)
        }
        view.findViewById<ImageButton>(R.id.image_before_gallery).setOnClickListener {
            typeImage = "BEFORE"
            fromImage = "GALLERY"
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent,
                CODE_RESULT_GALLERY
            )
        }
        view.findViewById<ImageButton>(R.id.image_after).setOnClickListener {
            typeImage = "AFTER"
            fromImage = "CAMERA"
            dispatchTakePictureIntent(CODE_RESULT_CAMERA_MERCHANT_VISITED)
        }
        view.findViewById<ImageButton>(R.id.image_after_gallery).setOnClickListener {
            typeImage = "AFTER"
            fromImage = "GALLERY"
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,
                CODE_RESULT_GALLERY
            )
        }
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
                val f = saveBitmapToFile(File(currentPhotoPath))
                if (f != null) {
                    mainViewModel.uploadSelfie(f, SharedPrefsCache(this).getToken())
                } else {
                    dialogLoading.dismiss()
                    Toast.makeText(this, "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                }
            }
            CODE_RESULT_CAMERA_MERCHANT_VISITED -> if (resultCode == RESULT_OK) {
                dialogLoading.show()
                val f = saveBitmapToFile(File(currentMerchantPhotoPath))
                if (f != null) {
                    merchantViewModel.uploadWithBD(f, pointSale.visitId, typeImage, SharedPrefsCache(this).getToken())
                } else {
                    dialogLoading.dismiss()
                    Toast.makeText(this, "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                }
            }
            CODE_RESULT_GALLERY -> if (resultCode == RESULT_OK) {
                val imageSelected: Uri? = imageReturnedIntent?.data
                if (imageSelected != null) {
                    currentMerchantPhotoUri = imageSelected
                    try {
                        val f = uriToImageFile(imageSelected)
                        if (f != null) {
                            dialogLoading.show()
                            merchantViewModel.uploadWithBD(f, pointSale.visitId, typeImage, SharedPrefsCache(this).getToken())
                        } else {
                            Toast.makeText(this, "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: FileNotFoundException) {
                        Toast.makeText(this, "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uriToImageFile(uri: Uri): File? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val filePath = cursor.getString(columnIndex)
                cursor.close()
                return File(filePath)
            }
            cursor.close()
        }
        return null
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

    private fun saveBitmapToFile(file: File): File? {
        return try { // BitmapFactory options to downsize the image
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            // factor of downsizing the image
            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()
            // The new size we want to scale to
            val REQUIRED_SIZE = 75
            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE
            ) {
                scale *= 2
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)
            val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream.close()
            // here i override the original image file
            file.createNewFile()
            val outputStream = FileOutputStream(file)
            selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            file
        } catch (e: Exception) {
            null
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
            if (code == CODE_RESULT_CAMERA_SELFIE)
                currentPhotoPath = absolutePath
            else
                currentMerchantPhotoPath = absolutePath
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
                if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionCamera = true
                } else {
                    Toast.makeText(this, "Por favor considere en dar permisos de cámara.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}