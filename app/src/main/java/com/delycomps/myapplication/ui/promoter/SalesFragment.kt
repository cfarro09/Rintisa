package com.delycomps.myapplication.ui.promoter

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
import com.delycomps.myapplication.adapter.AdapterMerchandise
import com.delycomps.myapplication.adapter.AdapterSale
import com.delycomps.myapplication.model.Merchandise
import com.delycomps.myapplication.model.SurveyProduct
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SalesFragment : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var rvMerchandise: RecyclerView
    private lateinit var viewModel: PromoterViewModel
    private var listProductsSelected: MutableList<SurveyProduct> = ArrayList()
    private lateinit var listProduct: List<SurveyProduct>
    private lateinit var listMerchandise: List<Merchandise>
    private lateinit var listBrand: List<String>
    private val listMeasureUnit = listOf("KILO", "SACO", "UNIDAD")

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

        rvMerchandise = view.findViewById(R.id.rv_merchandise)
        rvMerchandise.layoutManager = LinearLayoutManager(view.context)

        viewModel.loadingInital.observe(requireActivity()) {
            if (it == false) {
                listProduct = viewModel.dataProducts.value ?: emptyList()
                listMerchandise = viewModel.dataMerchandise.value ?: emptyList()
                starALL(view)
            }
        }
    }

    private fun starALL (view : View) {
        listBrand = listOf("Seleccione").union(listProduct.map { it.brand!! }.distinct().toList()).toMutableList()

        val builderDialogMaterial: AlertDialog.Builder = AlertDialog.Builder(view.context)

        val inflater = this.layoutInflater
        val dialogProductUI = inflater.inflate(R.layout.layout_sale_register, null)
        builderDialogMaterial.setView(dialogProductUI)
        val dialogMaterial = builderDialogMaterial.create()
        manageDialogMaterial(dialogProductUI, dialogMaterial)

        val buttonRegister = view.findViewById<FloatingActionButton>(R.id.sale_register)

        rvMerchandise.adapter = AdapterMerchandise(listMerchandise.toMutableList(), object : AdapterMerchandise.ListAdapterListener {
            override fun onUpdateMerchandise(merchandise: Merchandise, position: Int) {
                viewModel.updateMerchandise(merchandise, position)
            }
        })

        rv.adapter = AdapterSale(listProductsSelected, object : AdapterSale.ListAdapterListener {
            override fun onClickAtDetailProduct(surveyProduct: SurveyProduct, position: Int, type: String) {
                indexSelected = position
                if (type == "UPDATE") {
                    val spinnerTmp = dialogProductUI.findViewById<Spinner>(R.id.spinner_product)
                    val listProductName = listProduct.filter { it.brand == surveyProduct.brand }.map { it.description }.toMutableList()
                    spinnerTmp.adapter = ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, listProductName)
                    spinnerTmp.setSelection(listProductName.indexOf(surveyProduct.description))

                    dialogProductUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(listBrand.indexOf(surveyProduct.brand))
                    dialogProductUI.findViewById<Spinner>(R.id.spinner_measure_unit).setSelection(listMeasureUnit.indexOf(surveyProduct.measureUnit))
                    dialogProductUI.findViewById<EditText>(R.id.dialog_quantity).text = Editable.Factory.getInstance().newEditable("" + surveyProduct.quantity)
                    dialogMaterial.show()
                } else {
                    (rv.adapter as AdapterSale).removeItemProduct(position)
                    viewModel.removeProduct(position)
                }
            }
        })

        buttonRegister.setOnClickListener {
            indexSelected = -1
            dialogProductUI.findViewById<Spinner>(R.id.spinner_product).adapter = ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, emptyList())
            dialogProductUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(0)
            dialogProductUI.findViewById<Spinner>(R.id.spinner_measure_unit).setSelection(0)
            dialogProductUI.findViewById<EditText>(R.id.dialog_quantity).text = Editable.Factory.getInstance().newEditable("")
            dialogMaterial.show()
        }
    }

    private fun manageDialogMaterial (view: View, dialog: AlertDialog) {
        val spinnerBrand = view.findViewById<Spinner>(R.id.spinner_brand)
        val spinnerProduct = view.findViewById<Spinner>(R.id.spinner_product)
        val spinnerMeasureUnit = view.findViewById<Spinner>(R.id.spinner_measure_unit)
        val editTextQuantity = view.findViewById<EditText>(R.id.dialog_quantity)

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
            val measureUnit = spinnerMeasureUnit.selectedItem?.toString() ?: ""
            val quantityString = editTextQuantity.text.toString()
            val quantity = if (quantityString == "") 0 else quantityString.toInt()

            if (product != "" && product != "Seleccione" && quantity > 0) {
                val productId = listProduct.find { it.description == product && it.brand == brand }?.productId ?: 0
                val surveyProduct = SurveyProduct(productId, product, brand, 0.0, measureUnit, quantity)
                if (indexSelected == -1) {
                    (rv.adapter as AdapterSale).addProduct(surveyProduct)
                    viewModel.addProduct(surveyProduct)
                }
                else {
                    (rv.adapter as AdapterSale).updateItemProduct(surveyProduct, indexSelected)
                    viewModel.updateProduct(surveyProduct, indexSelected)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(view.context, "Debe llenar todos los campos", Toast.LENGTH_LONG).show()
            }
        }

    }
}