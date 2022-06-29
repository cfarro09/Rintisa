package com.delycomps.myapplication.ui.supervisorPromoter

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.SupervisorViewModel
import com.delycomps.myapplication.adapter.AdapterCheck
import com.delycomps.myapplication.cache.Helpers
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.PointSale
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val CODE_RESULT_CAMERA = 10001

class StatusFragment : Fragment() {
    private lateinit var viewModel: SupervisorViewModel
    private var currentPhotoPath: String = ""
    private var numberImage: String = "0"
    private lateinit var dialogLoading: AlertDialog
    private lateinit var pointSale: PointSale

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_status, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(SupervisorViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        pointSale = activity?.intent?.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        val button = view.findViewById<ImageButton>(R.id.button_save)
        val editComment = view.findViewById<EditText>(R.id.text_comment)

        button.setOnClickListener {
            val comment = editComment.text.toString()
            if (comment != "") {
                val ob = JSONObject()
                ob.put("customerid", pointSale.customerId)
                ob.put("comment", comment)
                ob.put("type", "IMPULSADOR")

                viewModel.executeSupervisor(ob, "QUERY_UPDATE_COMMENT", SharedPrefsCache(view.context).getToken())
            }
        }

        viewModel.resExecute.observe(requireActivity()) {
            if (it.result == "QUERY_UPDATE_JSON_STATUS1") {
                if (!it.loading && it.success) {
//                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    val text = "Se actualizó correctmente (ESTADO PDV)"
                    Toast.makeText(view.context, text, Toast.LENGTH_LONG).show()
                } else if (!it.loading && !it.success) {
//                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    Toast.makeText(view.context, "Ocurrió un error inesperado", Toast.LENGTH_LONG).show()
                } else if (it.loading) {
//                    dialogLoading.show()
                }
            }
        }

        viewModel.urlImageWithBD.observe(requireActivity()) {
            if (!it.loading) {
                dialogLoading.dismiss()
                if (it.success) {
                    when (numberImage) {
                        "2" -> {
                            view.findViewById<ImageView>(R.id.status_image1).setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
                        }
                        "3" -> {
                            view.findViewById<ImageView>(R.id.status_image2).setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
                        }
                        "4" -> {
                            view.findViewById<ImageView>(R.id.status_image3).setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
                        }
                        "5" -> {
                            view.findViewById<ImageView>(R.id.status_image4).setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
                        }
                    }
                    Toast.makeText(view.context, "Se subió la imagen con exito!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(view.context, Constants.ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            }
        }

        val buttonSaveStatus: ImageButton = view.findViewById(R.id.button_save_status)

        view.findViewById<Button>(R.id.upload_image1).setOnClickListener { dispatchTakePictureIntent(view, "2") }
        view.findViewById<Button>(R.id.upload_image2).setOnClickListener { dispatchTakePictureIntent(view, "3") }
        view.findViewById<Button>(R.id.upload_image3).setOnClickListener { dispatchTakePictureIntent(view, "4") }
        view.findViewById<Button>(R.id.upload_image4).setOnClickListener { dispatchTakePictureIntent(view, "5") }

        val rvStatus: RecyclerView = view.findViewById(R.id.main_rv_status)
        rvStatus.layoutManager = LinearLayoutManager(view.context)

        rvStatus.adapter = AdapterCheck((viewModel.dataCheckSupPromoter.value?.filter { it.type == "PDV" }?.toMutableList() ?: ArrayList()))

        buttonSaveStatus.setOnClickListener {
            val ob = JSONObject()
            ob.put("customerid", pointSale?.customerId)
            val ob1 = JSONObject()
            (rvStatus.adapter as AdapterCheck).getList().forEach {
                ob1.put(it.key, it.flag)
            }
            ob.put("json", ob1.toString())
            ob.put("aux_userid", viewModel.userSelected.value)

            viewModel.executeSupervisor(ob, "QUERY_UPDATE_JSON_STATUS1", SharedPrefsCache(view.context).getToken())
        }
    }


    private fun dispatchTakePictureIntent(view: View, image: String) {
        numberImage = image
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


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        imageReturnedIntent: Intent?
    ){
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            CODE_RESULT_CAMERA -> if (resultCode == AppCompatActivity.RESULT_OK) {
                dialogLoading.show()
                val f = Helpers().saveBitmapToFile(File(currentPhotoPath))
                if (f != null) {
                    val ob = JSONObject()
                    ob.put("method", "QUERY_UPDATE_SUP_PROMOTER_IMAGE$numberImage")

                    val ob1 = JSONObject()
                    ob1.put("customerid", pointSale?.customerId)

                    ob.put("parameters", ob1)

                    viewModel.uploadWithBD(f, ob.toString(), SharedPrefsCache(requireContext()).getToken())
                } else {
                    dialogLoading.dismiss()
                    Toast.makeText(context, "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
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