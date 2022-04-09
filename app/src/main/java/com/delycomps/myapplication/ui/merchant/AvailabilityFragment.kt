package com.delycomps.myapplication.ui.merchant

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.adapter.AdapterPriceProduct
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.model.PriceProduct
import com.delycomps.myapplication.model.SurveyProduct


class AvailabilityFragment : Fragment() {
    private lateinit var pointSale: PointSale
    private lateinit var viewModel: MerchantViewModel
    private lateinit var rv: RecyclerView
    private lateinit var listProduct: List<SurveyProduct>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_price, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MerchantViewModel::class.java)

        viewModel.dataProducts.observe(requireActivity()) {
            listProduct = it
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rv_product_availability)
        rv.layoutManager = LinearLayoutManager(view.context)

        pointSale = requireActivity().intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

//        listProductsSelected = BDLocal(view.context).getMerchantPrices(pointSale.visitId).toMutableList()
//        viewModel.initialPriceProduct(listProductsSelected)

        val spinnerBrand = view.findViewById<Spinner>(R.id.spinner_product)
        spinnerBrand.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listProduct.filter { it.competence == "RINTI" }.map { it.brand }.distinct())

        val spinnerCompetence = view.findViewById<Spinner>(R.id.spinner_competence)
        spinnerCompetence.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("RINTI", "COMPETENCE"))

        spinnerCompetence.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view1: View?, position: Int, id: Long) {
                val valueSelected = spinnerCompetence.selectedItem.toString()
                spinnerBrand.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listProduct.filter { it.competence == valueSelected }.map { it.brand }.distinct())
            }
        }

        spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view1: View?, position: Int, id: Long) {
                val brand = spinnerBrand.selectedItem.toString()
                val competence = spinnerCompetence.selectedItem.toString()
                listProduct.filter { it.competence == competence && it.brand == brand }.map { it.description }
            }
        }


    }

}