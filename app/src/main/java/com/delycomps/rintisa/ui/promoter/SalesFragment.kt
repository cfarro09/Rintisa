package com.delycomps.rintisa.ui.promoter

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
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
import com.delycomps.rintisa.Constants
import com.delycomps.rintisa.R
import com.delycomps.rintisa.adapter.AdapterSale
import com.delycomps.rintisa.cache.Helpers
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.model.BrandSale
import com.delycomps.rintisa.model.Merchandise
import com.delycomps.rintisa.model.PointSale
import com.delycomps.rintisa.model.SurveyProduct
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private var lastpath: String = ""
    private lateinit var dialogLoading: AlertDialog
    private lateinit var imageRegister: ImageView
    private val listRows = listOf("1 REGISTRO", "2 REGISTROS", "3 REGISTROS", "4 REGISTROS", "5 REGISTROS")
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
        val linearLayoutManager = LinearLayoutManager(view.context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        rv.layoutManager = linearLayoutManager

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

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
            override fun onClickAtDetailProduct(sp: SurveyProduct, position: Int, type: String) {
                indexSelected = position
                if (type == "UPDATE") {

                    dialogProductUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(listBrand.indexOf(sp.brand))

                    val measures: List<String> = listBrandSale.find { it.brand == sp.brand }?.listMeasure ?: emptyList()

                    dialogProductUI.findViewById<Spinner>(R.id.spinner_measure_unit).adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, measures)


                    Handler().postDelayed({
                        dialogProductUI.findViewById<Spinner>(R.id.spinner_measure_unit).setSelection(measures.indexOfFirst { it == sp.measureUnit })
                    }, 100)

                    dialogProductUI.findViewById<Spinner>(R.id.spinner_count_rows).visibility = View.GONE
                    dialogProductUI.findViewById<EditText>(R.id.dialog_quantity).text = Editable.Factory.getInstance().newEditable("" + sp.quantity)

                    lastpath = sp.imageEvidenceLocal ?: ""
                    if ((sp.imageEvidenceLocal ?: "") != "") {
                        imageRegister.setImageBitmap(BitmapFactory.decodeFile(sp.imageEvidenceLocal))
                    } else {
                        imageRegister.setImageDrawable(null);
                    }
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
            indexSelected = -1
            dialogProductUI.findViewById<Spinner>(R.id.spinner_count_rows).visibility = View.VISIBLE
            dialogProductUI.findViewById<Spinner>(R.id.spinner_count_rows).setSelection(0)
            dialogProductUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(1)
            dialogProductUI.findViewById<Spinner>(R.id.spinner_measure_unit).setSelection(0)
            dialogProductUI.findViewById<EditText>(R.id.dialog_quantity).text = Editable.Factory.getInstance().newEditable("1")
            lastpath = ""
            imageRegister.setImageDrawable(null);
            dialogMaterial.show()
        }
    }

    private fun manageDialogMaterial (view: View, dialog: AlertDialog) {
        val spinnerBrand = view.findViewById<Spinner>(R.id.spinner_brand)
        val spinnerCountRows = view.findViewById<Spinner>(R.id.spinner_count_rows)
        val spinnerMeasureUnit = view.findViewById<Spinner>(R.id.spinner_measure_unit)
        val editTextQuantity = view.findViewById<EditText>(R.id.dialog_quantity)
        val buttonTakePhoto = view.findViewById<Button>(R.id.dialog_take_photo_product)

        imageRegister = view.findViewById(R.id.view_image_evidence)

        buttonTakePhoto.setOnClickListener {
            dispatchTakePictureIntent(view)
        }
        spinnerCountRows.adapter = ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, listRows)
        spinnerBrand.adapter = object : ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, listBrand) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }

        spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val brand = spinnerBrand.selectedItem.toString()
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
            val countRow = spinnerCountRows.selectedItemPosition
            val measureUnit = spinnerMeasureUnit.selectedItem?.toString() ?: ""
            val quantityString = editTextQuantity.text.toString()
            val quantity = if (quantityString == "") 0.0 else quantityString.toDouble()

            if (brand != "" && brand != "Seleccione" && quantity > 0) {
                if (indexSelected == -1) {
                    var listProducts1 = emptyList<SurveyProduct>()
                    for (i in 0..countRow) {
                        val surveyProduct = SurveyProduct(0, brand, brand, 0.0, measureUnit, quantity, "", "")
                        surveyProduct.imageEvidenceLocal = lastpath
                        listProducts1 = viewModel.addProduct(surveyProduct, pointSale.visitId, view.context)
                        (rv.adapter as AdapterSale).addProduct(surveyProduct)
                    }
                    viewModel.updateSales(pointSale.visitId, listProducts1, SharedPrefsCache(view.context).getToken())
                }
                else {
                    val surveyProduct = SurveyProduct(0, brand, brand, 0.0, measureUnit, quantity, "", "")
                    surveyProduct.imageEvidenceLocal = lastpath
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        imageReturnedIntent: Intent?
    ){
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            CODE_RESULT_CAMERA -> if (resultCode == AppCompatActivity.RESULT_OK) {
                val f = Helpers().saveBitmapToFile(File(currentPhotoPath))
                if (f != null) {
                    lastpath = f.path
                    Log.d("caaa", f.path)
                    imageRegister.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
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