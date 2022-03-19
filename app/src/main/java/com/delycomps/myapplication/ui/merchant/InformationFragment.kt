package com.delycomps.myapplication.ui.merchant

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.PointSale
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val CODE_RESULT_CAMERA = 10001
private const val CODE_RESULT_GALLERY = 10002

class InformationFragment : Fragment() {
    private lateinit var viewModel: MerchantViewModel
    private var currentPhotoPath: String = ""
    private lateinit var currentPhotoUri: Uri
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
            dispatchTakePictureIntent(view)
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
        val pointSale: PointSale? = activity?.intent?.getParcelableExtra(Constants.POINT_SALE_ITEM)
        if (pointSale != null) {
            view.findViewById<TextView>(R.id.pdv_client).text = pointSale.client
            view.findViewById<TextView>(R.id.pdv_market).text = pointSale.market
            view.findViewById<TextView>(R.id.pdv_stall_number).text = "N° PUESTO: " + pointSale.stallNumber
            view.findViewById<TextView>(R.id.pdv_date).text = "${pointSale.visitFrequency} (${pointSale.visitDay})"
            view.findViewById<TextView>(R.id.pdv_last_visit).text = pointSale.lastVisit

            val color = if (pointSale.trafficLights == "AMARILLO") "#FFF8B7" else if (pointSale.trafficLights == "VERDE") "#B6FFA9" else  "#FF9696"
            view.findViewById<View>(R.id.pdv_traffic_light).backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
        }

        viewModel.urlAfterImage.observe(requireActivity()) {
            dialogLoading.dismiss()
            if (it == "") {
                Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
            } else {
                if (fromImage == "CAMERA") {
                    view.findViewById<ImageView>(R.id.view_image_after).setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
                } else {
                    view.findViewById<ImageView>(R.id.view_image_after).setImageURI(currentPhotoUri)
                }
//                Glide.with(view.context)
//                    .load(it)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .circleCrop()
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(view.findViewById(R.id.view_image_after))
            }
        }
        viewModel.urlBeforeImage.observe(requireActivity()) {
            dialogLoading.dismiss()
            if (it == "") {
                Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
            } else {
                if (fromImage == "CAMERA") {
                    view.findViewById<ImageView>(R.id.view_image_before).setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
                } else {
                    view.findViewById<ImageView>(R.id.view_image_before).setImageURI(currentPhotoUri)
                }
//                Glide.with(view.context)
//                    .load(it)
//                    .circleCrop()
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .placeholder(R.drawable.ic_baseline_person_24)
//
//                    .into(view.findViewById(R.id.view_image_before))
            }
        }
    }

    private fun uriToImageFile(uri: Uri): File? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, filePathColumn, null, null, null)
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        imageReturnedIntent: Intent?
    ){
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            CODE_RESULT_CAMERA -> if (resultCode == AppCompatActivity.RESULT_OK) {
                dialogLoading.show()
                val f = saveBitmapToFile(File(currentPhotoPath))
                if (f != null) {
                    if (typeImage == "BEFORE") {
                        viewModel.uploadBeforeImage(f, SharedPrefsCache(requireContext()).getToken())
                    } else {
                        viewModel.uploadAfterImage(f, SharedPrefsCache(requireContext()).getToken())
                    }
                } else {
                    dialogLoading.dismiss()
                    Toast.makeText(context, "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                }
            }
            CODE_RESULT_GALLERY -> if (resultCode == AppCompatActivity.RESULT_OK) {
                val imageSelected: Uri? = imageReturnedIntent?.data
                if (imageSelected != null) {
                    currentPhotoUri = imageSelected
                    try {
                        val f = uriToImageFile(imageSelected)
                        if (f != null) {
                            dialogLoading.show()
                            if (typeImage == "BEFORE") {
                                viewModel.uploadBeforeImage(f, SharedPrefsCache(requireContext()).getToken())
                            } else {
                                viewModel.uploadAfterImage(f, SharedPrefsCache(requireContext()).getToken())
                            }
                        } else {
                            Toast.makeText(requireContext(), "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: FileNotFoundException) {
                        Toast.makeText(requireContext(), "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
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