package com.delycomps.myapplication.ui.promoter

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.adapter.AdapterSale
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.BrandSale
import com.delycomps.myapplication.model.Merchandise
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.model.SurveyProduct
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val CODE_RESULT_CAMERA = 10001
private const val CODE_RESULT_GALLERY = 10002

class SalesFragment : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var viewModel: PromoterViewModel
    private var listProductsSelected: MutableList<SurveyProduct> = ArrayList()
    private lateinit var listBrandSale: List<BrandSale>
    private lateinit var listMerchandise: List<Merchandise>
    private lateinit var listBrand: List<String>
    private var currentPhotoPath: String = ""
    private lateinit var dialogLoading: AlertDialog
    private lateinit var imageRegister: ImageView
    private lateinit var loadingEvidence: ProgressBar
//    private val listMeasureUnit = listOf("KILO", "SACO", "HUMEDO", "SNACK")
    private lateinit var pointSale: PointSale
    private var indexSelected = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sales, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(PromoterViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rv_sale)
        rv.layoutManager = LinearLayoutManager(view.context)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        viewModel.loadingSelfie.observe(requireActivity()) {
            if (it == true) {
                dialogLoading.dismiss()
                if (viewModel.urlSelfie.value != "") {
                    imageRegister.visibility = View.VISIBLE
                    loadingEvidence.visibility = View.VISIBLE
                    Glide.with(view.context)
                        .load(viewModel.urlSelfie.value)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                loadingEvidence.visibility = View.GONE
                                return false
                            }
                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                loadingEvidence.visibility = View.GONE
                                return false
                            }
                        })
                        .into(imageRegister)

                } else {
                    Toast.makeText(view.context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show()
                }
            }
        }
        pointSale = requireActivity().intent.getParcelableExtra<PointSale>(Constants.POINT_SALE_ITEM)!!

        listBrandSale = viewModel.dataBrandSale.value ?: emptyList()
        listMerchandise = viewModel.dataMerchandise.value ?: emptyList()
        listProductsSelected = viewModel.listProductSelected.value ?: ArrayList()
        starALL(view)
    }

    private fun starALL (view : View) {
        listBrand = listOf("Seleccione").union(listBrandSale.map { it.brand }.toList()).toMutableList()

        val builderDialogMaterial: AlertDialog.Builder = AlertDialog.Builder(view.context)

        val inflater = this.layoutInflater
        val dialogProductUI = inflater.inflate(R.layout.layout_sale_register, null)
        builderDialogMaterial.setView(dialogProductUI)
        val dialogMaterial = builderDialogMaterial.create()
        manageDialogMaterial(dialogProductUI, dialogMaterial)

        val buttonRegister = view.findViewById<FloatingActionButton>(R.id.sale_register)

        rv.adapter = AdapterSale(listProductsSelected, listMerchandise, object : AdapterSale.ListAdapterListener {
            override fun onClickAtDetailProduct(surveyProduct: SurveyProduct, position: Int, type: String) {
                indexSelected = position
                if (type == "UPDATE") {
                    viewModel.setUrlSelfie(surveyProduct.imageEvidence ?: "")
                    dialogProductUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(listBrand.indexOf(surveyProduct.brand))
                    dialogProductUI.findViewById<EditText>(R.id.dialog_quantity).text = Editable.Factory.getInstance().newEditable("" + surveyProduct.quantity)

                    imageRegister.visibility = View.VISIBLE
                    Glide.with(view.context)
                        .load(viewModel.urlSelfie.value)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                loadingEvidence.visibility = View.GONE
                                return false
                            }
                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                loadingEvidence.visibility = View.GONE
                                return false
                            }
                        })
                        .into(imageRegister)

                    dialogMaterial.show()
                } else {
                    val listProducts = viewModel.removeProduct(position, view.context)
                    (rv.adapter as AdapterSale).removeItemProduct(position)
                    viewModel.updateSales(pointSale.visitId, listProducts, SharedPrefsCache(view.context).getToken())
                }
            }
        }, object : AdapterSale.ListAdapterListenerMerchant {
            override fun onChangeMerchant(surveyProduct: SurveyProduct, position: Int) {
                val listProducts = viewModel.updateProduct(surveyProduct, position, view.context)
                viewModel.updateSales(pointSale.visitId, listProducts, SharedPrefsCache(view.context).getToken())
            }
        })

        buttonRegister.setOnClickListener {
            viewModel.setUrlSelfie("")
            indexSelected = -1
            dialogProductUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(0)
            dialogProductUI.findViewById<Spinner>(R.id.spinner_measure_unit).setSelection(0)
            dialogProductUI.findViewById<EditText>(R.id.dialog_quantity).text = Editable.Factory.getInstance().newEditable("")

            loadingEvidence.visibility = View.GONE
            imageRegister.visibility = View.GONE
            dialogMaterial.show()
        }
    }

    private fun manageDialogMaterial (view: View, dialog: AlertDialog) {
        val spinnerBrand = view.findViewById<Spinner>(R.id.spinner_brand)
        val spinnerMeasureUnit = view.findViewById<Spinner>(R.id.spinner_measure_unit)
        val editTextQuantity = view.findViewById<EditText>(R.id.dialog_quantity)
        val buttonTakePhoto = view.findViewById<Button>(R.id.dialog_take_photo_product)
        val buttonSelectImage = view.findViewById<Button>(R.id.dialog_select_gallery_product)
        loadingEvidence = view.findViewById(R.id.loading_evidence)
        imageRegister = view.findViewById(R.id.view_image_evidence)

        buttonTakePhoto.setOnClickListener {
            dispatchTakePictureIntent(view)
        }
        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, CODE_RESULT_GALLERY)
        }

        spinnerBrand.adapter = object : ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, listBrand) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }

        spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val brand = spinnerBrand.selectedItem.toString()
//                spinnerProduct.adapter = ArrayAdapter<String?>(view!!.context, android.R.layout.simple_list_item_1, listProduct.filter { it.brand == brand }.map { it.description }.toMutableList())
                val measures: List<String> = listBrandSale.find { it.brand == brand }?.listMeasure ?: emptyList()
                spinnerMeasureUnit.adapter = ArrayAdapter(view!!.context, android.R.layout.simple_list_item_1, measures)
            }
        }

        val buttonSave = view.findViewById<Button>(R.id.dialog_save_product)
        val buttonCancel = view.findViewById<Button>(R.id.dialog_cancel_product)

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonSave.setOnClickListener {
            val brand = spinnerBrand.selectedItem?.toString() ?: ""
            val measureUnit = spinnerMeasureUnit.selectedItem?.toString() ?: ""
            val quantityString = editTextQuantity.text.toString()
            val quantity = if (quantityString == "") 0 else quantityString.toInt()

            if (brand != "" && brand != "Seleccione" && quantity > 0) {

                val surveyProduct = SurveyProduct(0, brand, brand, 0.0, measureUnit, quantity, "", viewModel.urlSelfie.value)
                if (indexSelected == -1) {
                    val listProducts = viewModel.addProduct(surveyProduct, pointSale.visitId, view.context)
                    (rv.adapter as AdapterSale).addProduct(surveyProduct)
                    viewModel.updateSales(pointSale.visitId, listProducts, SharedPrefsCache(view.context).getToken())
                }
                else {
                    (rv.adapter as AdapterSale).updateItemProduct(surveyProduct, indexSelected)
                    val listProducts = viewModel.updateProduct(surveyProduct, indexSelected, view.context)
                    viewModel.updateSales(pointSale.visitId, listProducts, SharedPrefsCache(view.context).getToken())
                }
                dialog.dismiss()
            } else {
                Toast.makeText(view.context, "Debe llenar todos los campos", Toast.LENGTH_LONG).show()
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
                    viewModel.uploadSelfie(f, SharedPrefsCache(requireContext()).getToken())
                } else {
                    dialogLoading.dismiss()
                    Toast.makeText(requireContext(), "Hubo un error al procesar la foto", Toast.LENGTH_SHORT).show()
                }
            }
            CODE_RESULT_GALLERY -> if (resultCode == AppCompatActivity.RESULT_OK) {
                val imageSelected: Uri? = imageReturnedIntent?.data
                if (imageSelected != null) {
                    try {
                        val imageOriginal = uriToImageFile(imageSelected)
                        if (imageOriginal != null) {
                            dialogLoading.show()
                            viewModel.uploadSelfie(imageOriginal, SharedPrefsCache(requireContext()).getToken())
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
            Log.d("error_carlos", e.message ?: "")
            null
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
                    startActivityForResult(takePictureIntent, CODE_RESULT_CAMERA)
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