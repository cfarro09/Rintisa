package com.delycomps.rintisa

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
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.adapter.AdapterQuestionDynamic
import com.delycomps.rintisa.cache.Helpers
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.model.Customer
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
private const val CODE_RESULT_CAMERA = 10001

class AuditorClientFragment : Fragment() {
    private var currentPhotoPath: String = ""
    private var numberImage: String = "0"
    private lateinit var viewModel: AuditorViewModel
    private lateinit var dialogLoading: AlertDialog
    private lateinit var customer: Customer
    private var status2 = "EN ESPERA"
    private lateinit var view1: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_auditor_client, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(AuditorViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()
        view1 = view
        customer = activity?.intent?.getParcelableExtra(Constants.POINT_CUSTOMER)!!

        val button = view.findViewById<ImageButton>(R.id.button_save)

        val rvBins: RecyclerView = view.findViewById(R.id.main_rv_bins)
        rvBins.layoutManager = LinearLayoutManager(view.context)

        view.findViewById<Button>(R.id.upload_image1).setOnClickListener { dispatchTakePictureIntent(view, "1") }
        view.findViewById<Button>(R.id.upload_image2).setOnClickListener { dispatchTakePictureIntent(view, "2") }

        val listQuestions = viewModel.questionClients.value ?: arrayListOf()
        var lasttype = ""
        for (ii in listQuestions.indices) {
            val tt = listQuestions[ii].type
            if (lasttype == "") {
                lasttype = listQuestions[ii].type
            } else {
                if (lasttype == listQuestions[ii].type) {
                    listQuestions[ii].type = ""
                }
                lasttype = tt
            }
        }
        rvBins.adapter = AdapterQuestionDynamic(listQuestions.toMutableList())

        button.setOnClickListener {
            val ob = JSONObject()
            ob.put("customerid", customer.customerId)
            val ob1 = JSONObject()
            ob.put("image1", viewModel.image1.value ?: "")
            ob.put("image2", viewModel.image2.value ?: "")
            (rvBins.adapter as AdapterQuestionDynamic).getList().forEach {
                val key = it.key.split("||")[0]
                val type = it.key.split("||")[1]
                if (type == "switch") {
                    ob1.put(key, if (it.flag) "SI" else "NO")
                } else {
                    ob1.put(key, it.value)
                }
            }
            ob.put("json_content", ob1.toString())

            viewModel.executeSupervisor(ob, "UFN_AUDITION_RINTISA_CLIENT_INS2", SharedPrefsCache(view.context).getToken())
        }

        viewModel.loading.observe(requireActivity()) {
            if (it) {
                dialogLoading.show()
            } else {
                dialogLoading.dismiss()
            }
        }

        viewModel.resExecute.observe(requireActivity()) {
            if (it.result == "UFN_AUDITION_RINTISA_CLIENT_INS2") {
                if (!it.loading && it.success) {
//                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    status2 = "GESTIONADO"
                    val text = "Se insertó correctamente"
                    Toast.makeText(view.context, text, Toast.LENGTH_LONG).show()

                    val output = Intent()
                    output.putExtra("status", "GESTIONADO")
                    requireActivity().setResult(AppCompatActivity.RESULT_OK, output);
                    requireActivity().finish()

                } else if (!it.loading && !it.success) {
//                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    Toast.makeText(view.context, "Ocurrió un error inesperado", Toast.LENGTH_LONG).show()
                } else if (it.loading) {
//                    dialogLoading.show()
                }
            }
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
                when (numberImage) {
                    "1" -> {
                        view1.findViewById<ImageView>(R.id.status_image1).setImageBitmap(
                            BitmapFactory.decodeFile(currentPhotoPath))
                    }
                    "2" -> {
                        view1.findViewById<ImageView>(R.id.status_image2).setImageBitmap(
                            BitmapFactory.decodeFile(currentPhotoPath))
                    }
                }
                viewModel.uploadImage(Helpers().saveBitmapToFile(File(currentPhotoPath))!!, numberImage, SharedPrefsCache(view1.context).getToken())
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