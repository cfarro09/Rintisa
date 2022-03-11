package com.delycomps.myapplication.ui.merchant

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.R
import com.delycomps.myapplication.adapter.AdapterProductSurvey
import com.delycomps.myapplication.model.SurveyProduct
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PriceFragment : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var viewModel: MerchantViewModel
    private var listProductsSelected: MutableList<SurveyProduct> = ArrayList()
    private lateinit var listProduct: List<SurveyProduct>
    private lateinit var listBrand: List<String>
    private val listMeasureUnit = listOf("KILO", "SACO", "UNIDAD")

    private var indexSelected = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_price, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MerchantViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.main_rv_survey_price)
        rv.layoutManager = LinearLayoutManager(view.context)

        viewModel.dataProducts.observe(requireActivity()) {
            listProduct = it
            starALL(view)
        }
    }

    private fun starALL (view : View) {
        listBrand = listOf("Seleccione").union(listProduct.map { it.brand!! }.distinct().toList()).toMutableList()

        val builderDialogMaterial: AlertDialog.Builder = AlertDialog.Builder(view.context)

        val inflater = this.layoutInflater
        val dialogProductUI = inflater.inflate(R.layout.layout_product_register, null)
        builderDialogMaterial.setView(dialogProductUI)
        val dialogMaterial = builderDialogMaterial.create()
        manageDialogMaterial(dialogProductUI, dialogMaterial)

        val buttonRegister = view.findViewById<FloatingActionButton>(R.id.survey_price_register)

        rv.adapter = AdapterProductSurvey(listProductsSelected, object : AdapterProductSurvey.ListAdapterListener {
            override fun onClickAtDetailProduct(surveyProduct: SurveyProduct, position: Int, type: String) {
                indexSelected = position
                if (type == "UPDATE") {
                    val spinnerTmp = dialogProductUI.findViewById<Spinner>(R.id.spinner_product)
                    val listProductName = listProduct.filter { it.brand == surveyProduct.brand }.map { it.description }.toMutableList()
                    spinnerTmp.adapter = ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, listProductName)
                    spinnerTmp.setSelection(listProductName.indexOf(surveyProduct.description))

                    dialogProductUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(listBrand.indexOf(surveyProduct.brand))
                    dialogProductUI.findViewById<Spinner>(R.id.spinner_measure_unit).setSelection(listMeasureUnit.indexOf(surveyProduct.measureUnit))
                    dialogProductUI.findViewById<EditText>(R.id.dialog_price).text = Editable.Factory.getInstance().newEditable("" + surveyProduct.price)
                    dialogMaterial.show()
                } else {
                    (rv.adapter as AdapterProductSurvey).removeItemProduct(position)
                    viewModel.removeProduct(position)
                }
            }
        })

        buttonRegister.setOnClickListener {
            indexSelected = -1
            dialogProductUI.findViewById<Spinner>(R.id.spinner_product).adapter = ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, emptyList())
            dialogProductUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(0)
            dialogProductUI.findViewById<Spinner>(R.id.spinner_measure_unit).setSelection(0)
            dialogProductUI.findViewById<EditText>(R.id.dialog_price).text = Editable.Factory.getInstance().newEditable("")
            dialogMaterial.show()
        }
    }

    private fun manageDialogMaterial (view: View, dialog: AlertDialog) {
        val spinnerBrand = view.findViewById<Spinner>(R.id.spinner_brand)
        val spinnerProduct = view.findViewById<Spinner>(R.id.spinner_product)
        val spinnerMeasureUnit = view.findViewById<Spinner>(R.id.spinner_measure_unit)
        val editTextPrice = view.findViewById<EditText>(R.id.dialog_price)

        spinnerProduct.adapter = ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, emptyList())

        spinnerBrand.adapter = object : ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, listBrand) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }

        spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val valueSelected = spinnerBrand.selectedItem.toString()
                spinnerProduct.adapter = ArrayAdapter<String?>(view!!.context, android.R.layout.simple_list_item_1, listProduct.filter { it.brand == valueSelected }.map { it.description }.toMutableList())
            }
        }

        spinnerMeasureUnit.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listMeasureUnit)

        val buttonSave = view.findViewById<Button>(R.id.dialog_save_product)
        val buttonCancel = view.findViewById<Button>(R.id.dialog_cancel_product)

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonSave.setOnClickListener {
            val product = spinnerProduct.selectedItem?.toString() ?: ""
            val brand = spinnerBrand.selectedItem?.toString() ?: ""
            val measureUnit = spinnerMeasureUnit.selectedItem.toString()
            val priceString = editTextPrice.text.toString()
            val price = if (priceString == "") 0.00 else priceString.toDouble()

            if (product != "" && product != "Seleccione" && price > 0) {
                val productId = listProduct.find { it.description == product && it.brand == brand }?.productId ?: 0

                val surveyProduct = SurveyProduct(productId, product, brand, price, measureUnit, 0, "")
                if (indexSelected == -1) {
                    (rv.adapter as AdapterProductSurvey).addProduct(surveyProduct)
                    viewModel.addProduct(surveyProduct)
                }
                else {
                    (rv.adapter as AdapterProductSurvey).updateItemProduct(surveyProduct, indexSelected)
                    viewModel.updateProduct(surveyProduct, indexSelected)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(view.context, "Debe llenar todos los campos", Toast.LENGTH_LONG).show()
            }
        }

    }
}