package com.delycomps.myapplication.ui.merchant

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.cache.Helpers
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.Management
import com.delycomps.myapplication.model.PointSale
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


private const val CODE_RESULT_CAMERA = 10001
private const val CODE_RESULT_GALLERY = 10002
private val STATUS_LIST = listOf("EFECTIVA", "NO EFECTIVA")
private val MOTIVES_LIST = listOf("Cambio de rubro", "No permite el ingreso", "PDV cerrado", "No visitado")

class InformationFragment : Fragment() {
    private lateinit var viewModel: MerchantViewModel
    private var currentPhotoPath: String = ""
    private var currentPhotoBitmap: Bitmap? = null
    private var pointSale: PointSale? = null

    private var fromImage: String = ""
    private var typeImage: String = ""
    private lateinit var dialogLoading: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.information_fragment, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MerchantViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        view.findViewById<ImageButton>(R.id.image_before).setOnClickListener {
            typeImage = "BEFORE"
            fromImage = "CAMERA"
            dispatchTakePictureIntent(view)
        }
        view.findViewById<ImageButton>(R.id.image_after).setOnClickListener {
            typeImage = "AFTER"
            fromImage = "CAMERA"
            dispatchTakePictureIntent(view)
        }
        pointSale = activity?.intent?.getParcelableExtra(Constants.POINT_SALE_ITEM)
        if (pointSale != null) {

            val listpoint = BDLocal(view.context).getPointSaleOne(pointSale!!.visitId)

            if (listpoint.count() > 0) {
                val point = listpoint[0]
                if ((point.imageAfterLocal ?: "") != "") {
                    viewModel.uploadImageLocal(point.imageAfterLocal!!, "AFTER")
                    view.findViewById<ImageView>(R.id.view_image_after).setImageBitmap(BitmapFactory.decodeFile(point.imageAfterLocal!!))
                }
                if ((point.imageBeforeLocal ?: "") != "") {
                    viewModel.uploadImageLocal(point.imageBeforeLocal!!, "BEFORE")
                    view.findViewById<ImageView>(R.id.view_image_before).setImageBitmap(BitmapFactory.decodeFile(point.imageBeforeLocal!!))
                }
            }
            view.findViewById<TextView>(R.id.pdv_client).text = pointSale!!.client
            view.findViewById<TextView>(R.id.pdv_market).text = pointSale!!.market
            view.findViewById<TextView>(R.id.pdv_stall_number).text = "N° PUESTO: " + pointSale!!.stallNumber
            view.findViewById<TextView>(R.id.pdv_date).text = "${pointSale!!.visitFrequency} (${pointSale!!.visitDay})"
            view.findViewById<TextView>(R.id.pdv_last_visit).text = pointSale!!.lastVisit
            view.findViewById<TextView>(R.id.pdv_comment).text = pointSale!!.comment

            val color = if (pointSale!!.trafficLights == "AMARILLO") "#FFF8B7" else if (pointSale!!.trafficLights == "VERDE") "#B6FFA9" else  "#FF9696"
            view.findViewById<View>(R.id.pdv_traffic_light).backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
        }

        val spinnerStatus = view.findViewById<Spinner>(R.id.spinner_status_management)
        val spinnerMotive = view.findViewById<Spinner>(R.id.spinner_motive)
        val editObservation = view.findViewById<EditText>(R.id.observation)
        val containerMotive = view.findViewById<LinearLayout>(R.id.container_motive)

        editObservation.addTextChangedListener {
            viewModel.setManagement(Management(null, null, editObservation.text.toString()))
        }
        spinnerStatus.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, STATUS_LIST)
        spinnerMotive.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, MOTIVES_LIST)

        spinnerMotive.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val motive = spinnerMotive.selectedItem.toString()
                viewModel.setManagement(Management(null, motive, null))
            }
        }

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val status = spinnerStatus.selectedItem.toString()
                spinnerMotive.setSelection(0)
                if (status == "EFECTIVA") {
                    containerMotive.visibility = View.GONE
                    viewModel.setManagement(Management("EFECTIVA", "", null))
                } else {
                    containerMotive.visibility = View.VISIBLE
                    viewModel.setManagement(Management("NO EFECTIVA", "Cambio de rubro", null))
                }
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
            CODE_RESULT_CAMERA -> if (resultCode == AppCompatActivity.RESULT_OK) {
                val f = Helpers().saveBitmapToFile(File(currentPhotoPath))
                viewModel.uploadImageLocal(f!!.path, typeImage)
                BDLocal(requireContext()).updatePointSaleLocal(pointSale!!.visitId, null, null, if (typeImage == "AFTER") currentPhotoPath else null, if (typeImage != "AFTER") currentPhotoPath else null)
                if (typeImage == "BEFORE") {
                    requireActivity().findViewById<ImageView>(R.id.view_image_before).setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
                } else {
                    requireActivity().findViewById<ImageView>(R.id.view_image_after).setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
                }
            }
        }
    }

    private fun dispatchTakePictureIntent(view: View) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(view.context.packageManager).also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(view)
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        view.context,
                        "com.delycomps.rintisa.provider",
                        photoFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent,
                        CODE_RESULT_CAMERA
                    )
                }
            }
        }
    }


    private fun createImageFile(view: View): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = view.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
}