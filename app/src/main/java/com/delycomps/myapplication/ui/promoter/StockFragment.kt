package com.delycomps.myapplication.ui.promoter

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.adapter.AdapterStock
import com.delycomps.myapplication.adapter.AdapterStockProduct
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.Material
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.model.Stock
import com.delycomps.myapplication.model.SurveyProduct
import com.delycomps.myapplication.ui.merchant.MerchantViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson

class StockFragment : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var viewModel: PromoterViewModel

    private var listStockSelected: MutableList<Stock> = ArrayList()
    private lateinit var listProduct: List<Stock>
    private lateinit var listBrand: List<String>
    private lateinit var pointSale: PointSale
    private var indexSelected = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stock, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(PromoterViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rv_stock)
        rv.layoutManager = LinearLayoutManager(view.context)


//        viewModel.loadingInital.observe(requireActivity()) {
//            if (it == false) {
//                listProduct = viewModel.dataStocks.value ?: emptyList()
//                listStockSelected = (viewModel.listStockSelected.value ?: ArrayList())
//                starALL(view)
//            }
//        }
        pointSale = requireActivity().intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        listProduct = viewModel.dataStocks.value ?: emptyList()
        listStockSelected = viewModel.listStockSelected.value!!.toMutableList()
        starALL(view)

//        viewModel.listStockSelected.observe(requireActivity()) {
//            val listReady = it ?: emptyList()
//
//        }
    }

    private fun starALL (view : View) {
        listBrand = listOf("Seleccione").union(listProduct.map { it.brand!! }.distinct().toList()).toMutableList()

        val builderDialogMaterial: AlertDialog.Builder = AlertDialog.Builder(view.context)

        val inflater = this.layoutInflater
        val dialogProductUI = inflater.inflate(R.layout.layout_stock_promoter_register, null)
        builderDialogMaterial.setView(dialogProductUI)
        val dialogMaterial = builderDialogMaterial.create()
        manageDialogMaterial(dialogProductUI, dialogMaterial)

        val buttonRegister = view.findViewById<FloatingActionButton>(R.id.stock_register)

        rv.adapter = AdapterStock(listStockSelected, object : AdapterStock.ListAdapterListener {
            override fun onUpdateStock(stock: Stock, position: Int, type: String) {
                indexSelected = position
                if (type != "UPDATE") {
                    BDLocal(view.context).deleteStockPromoter(stock.uuid.toString())
                    (rv.adapter as AdapterStock).removeItemStock(position)
                    val listStock = viewModel.removeStock(position)
                    viewModel.updateStock(pointSale.visitId, Gson().toJson(listStock), SharedPrefsCache(view.context).getToken())
                }
            }
        })

        buttonRegister.setOnClickListener {
            indexSelected = -1
            dialogProductUI.findViewById<Spinner>(R.id.spinner_type).adapter = ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, emptyList())
            dialogProductUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(0)
            dialogProductUI.findViewById<RecyclerView>(R.id.rv_products_stock).adapter = AdapterStockProduct(ArrayList())
            dialogMaterial.show()
        }
    }


    private fun manageDialogMaterial (view: View, dialog: AlertDialog) {
        val spinnerBrand = view.findViewById<Spinner>(R.id.spinner_brand)
        val spinnerType = view.findViewById<Spinner>(R.id.spinner_type)
        val rvProduct = view.findViewById<RecyclerView>(R.id.rv_products_stock)
        rvProduct.layoutManager = LinearLayoutManager(view.context)

//        spinnerProduct.adapter = ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, emptyList())

        spinnerBrand.adapter = object : ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, listBrand) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }

        spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val valueSelected = spinnerBrand.selectedItem.toString()
                spinnerType.adapter = ArrayAdapter<String?>(view!!.context, android.R.layout.simple_list_item_1, listProduct.filter { it.brand == valueSelected }.map { it.type }.distinct().toMutableList())
            }
        }

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val valueSelected = spinnerType.selectedItem.toString()
                val valueBrand = spinnerBrand.selectedItem.toString()
                rvProduct.adapter = AdapterStockProduct(listProduct.filter { it.brand == valueBrand && it.type == valueSelected }.toMutableList())
            }
        }

        val buttonSave = view.findViewById<Button>(R.id.dialog_save_product)
        val buttonCancel = view.findViewById<Button>(R.id.dialog_cancel_product)

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonSave.setOnClickListener {
            val listStockSelected = (rvProduct.adapter as AdapterStockProduct).getList().filter { it.flag }

            if (listStockSelected.count() > 0) {
                listStockSelected.forEach { r ->
                    run {
                        val insert = (rv.adapter as AdapterStock).addStock(r)
                        if (insert)
                            BDLocal(view.context).addStockPromoter(r, pointSale.visitId)
                    }
                }
                val listStock = viewModel.addStocks(listStockSelected)
                viewModel.updateStock(pointSale.visitId, Gson().toJson(listStock), SharedPrefsCache(view.context).getToken())
                dialog.dismiss()
            } else {
                Toast.makeText(view.context, "Debe seleccionar al menos un producto", Toast.LENGTH_LONG).show()
            }
        }
    }
}