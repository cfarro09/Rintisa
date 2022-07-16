package com.delycomps.rintisa.ui.merchant

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
import com.delycomps.rintisa.Constants
import com.delycomps.rintisa.R
import com.delycomps.rintisa.adapter.AdapterPriceProduct
import com.delycomps.rintisa.adapter.AdapterProductSurvey
import com.delycomps.rintisa.cache.BDLocal
import com.delycomps.rintisa.model.PointSale
import com.delycomps.rintisa.model.PriceProduct
import com.delycomps.rintisa.model.SurveyProduct
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PriceFragment : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var viewModel: MerchantViewModel
    private var listProductsSelected: MutableList<SurveyProduct> = ArrayList()
    private lateinit var listProduct: List<SurveyProduct>
    private lateinit var listBrand: List<String>
    private lateinit var pointSale: PointSale
    private val listMeasureUnit = listOf("KILO", "SACO")

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

        pointSale = requireActivity().intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        listProductsSelected = viewModel.listProductSelected.value ?: ArrayList()
//        listProductsSelected = BDLocal(view.context).getMerchantPrices(pointSale.visitId).toMutableList()
//        viewModel.initialPriceProduct(listProductsSelected)

        viewModel.dataProducts.observe(requireActivity()) {
            listProduct = it //.filter { r -> r.competence == "RINTI" }
            starALL(view)
        }
    }

    private fun starALL (view : View) {
        listBrand = listOf("Seleccione").union(listProduct.map { it.brand!! }.distinct().toList()).toMutableList()

        val builderDialogMaterial: AlertDialog.Builder = AlertDialog.Builder(view.context)
        val builderDialogMaterialAll: AlertDialog.Builder = AlertDialog.Builder(view.context)

        val inflater = this.layoutInflater
        val dialogProductUI = inflater.inflate(R.layout.layout_product_register, null)
        builderDialogMaterial.setView(dialogProductUI)
        val dialogMaterial = builderDialogMaterial.create()

        val inflaterAll = this.layoutInflater
        val dialogProductUIAll = inflaterAll.inflate(R.layout.layout_product_register_all, null)
        builderDialogMaterialAll.setView(dialogProductUIAll)
        val dialogMaterialAll = builderDialogMaterialAll.create()


        manageDialogMaterial(dialogProductUI, dialogMaterial)

        manageDialogMaterialAll(dialogProductUIAll, dialogMaterialAll)

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
                    BDLocal(view.context).deleteMerchantPrices(surveyProduct.uuid.toString())
                }
            }
        })

        buttonRegister.setOnClickListener {
            indexSelected = -1
            dialogProductUIAll.findViewById<Spinner>(R.id.spinner_brand).setSelection(0)
            dialogMaterialAll.show()
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

                val surveyProduct = SurveyProduct(productId, product, brand, price, measureUnit, 0.0, "")
                if (indexSelected != -1) {
                    (rv.adapter as AdapterProductSurvey).updateItemProduct(surveyProduct, indexSelected)
                    viewModel.updateProduct(surveyProduct, indexSelected, view.context)

                }
                dialog.dismiss()
            } else {
                Toast.makeText(view.context, "Debe llenar todos los campos", Toast.LENGTH_LONG).show()
            }
        }

    }


    private fun manageDialogMaterialAll (view: View, dialog: AlertDialog) {
        val spinnerCompetence = view.findViewById<Spinner>(R.id.spinner_competence)
        spinnerCompetence.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("RINTI", "COMPETENCIA"))

        val spinnerBrand = view.findViewById<Spinner>(R.id.spinner_brand)
        val rvProduct = view.findViewById<RecyclerView>(R.id.rv_products)
        rvProduct.layoutManager = LinearLayoutManager(view.context)

        spinnerCompetence.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view1: View?, position: Int, id: Long) {
                val valueSelected = spinnerCompetence.selectedItem.toString()
                spinnerBrand.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listProduct.filter { it.competence == valueSelected }.map { it.brand }.distinct())
            }
        }

        spinnerBrand.adapter = object : ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, listBrand) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val valueSelected = spinnerBrand.selectedItem.toString()
                val listProduct = listProduct.filter { it.brand == valueSelected }.map { PriceProduct(it.productId, it.description ?: "", 0.0, 0.0) }.toMutableList()
                rvProduct.adapter = AdapterPriceProduct(listProduct)
            }
        }
        val buttonSave = view.findViewById<Button>(R.id.dialog_save_product)
        val buttonCancel = view.findViewById<Button>(R.id.dialog_cancel_product)

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonSave.setOnClickListener {
            val brand = spinnerBrand.selectedItem?.toString() ?: ""
            val listProduct: MutableList<SurveyProduct> = ArrayList()
            (rvProduct.adapter as AdapterPriceProduct).getList().forEach {
                if (it.price_k > 0) {
                    listProduct.add(SurveyProduct(it.productId, it.description, brand, it.price_k, "KILO", 0.0))
                }
                if (it.price_s > 0) {
                    listProduct.add(SurveyProduct(it.productId, it.description, brand, it.price_s, "SACO", 0.0))
                }
            }
            if (listProduct.count() > 0) {
                viewModel.addProduct(listProduct)
                listProduct.forEach {
                    val insert = (rv.adapter as AdapterProductSurvey).addProduct(it)
                    if (insert) {
                        BDLocal(view.context).addMerchantPrice(it, pointSale.visitId)
                    }
                }
                dialog.dismiss()
            } else {
                Toast.makeText(view.context, "No tiene ningun producto editado", Toast.LENGTH_LONG).show()
            }
        }
    }
}