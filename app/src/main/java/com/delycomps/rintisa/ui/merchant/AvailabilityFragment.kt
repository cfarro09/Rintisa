package com.delycomps.rintisa.ui.merchant

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
import com.delycomps.rintisa.Constants
import com.delycomps.rintisa.R
import com.delycomps.rintisa.adapter.AdapterAvailability
import com.delycomps.rintisa.adapter.AdapterPriceProduct
import com.delycomps.rintisa.model.Availability
import com.delycomps.rintisa.model.PointSale
import com.delycomps.rintisa.model.PriceProduct
import com.delycomps.rintisa.model.SurveyProduct


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
        val view = inflater.inflate(R.layout.fragment_availability, container, false)
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

        val spinnerBrand = view.findViewById<Spinner>(R.id.spinner_brand)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinner_category)
        val spinnerPet = view.findViewById<Spinner>(R.id.spinner_pet)
        spinnerBrand.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listProduct.filter { it.competence == "RINTI" }.map { it.brand }.distinct())

        val spinnerCompetence = view.findViewById<Spinner>(R.id.spinner_competence)
        spinnerCompetence.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("RINTI", "COMPETENCIA"))

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
                val valueSelected = spinnerBrand.selectedItem.toString()
                val competence = spinnerCompetence.selectedItem.toString()
                spinnerCategory.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listProduct.filter { it.brand == valueSelected && it.competence == competence }.map { it.category }.distinct())
            }
        }

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view1: View?, position: Int, id: Long) {
                val valueSelected = spinnerCategory.selectedItem.toString()
                val brand = spinnerBrand.selectedItem.toString()

                spinnerPet.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listProduct.filter { it.category == valueSelected && it.brand == brand }.map { it.pet }.distinct())
            }
        }

        spinnerPet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = spinnerCategory.selectedItem.toString()
                val brand = spinnerBrand.selectedItem.toString()
                val pet = spinnerPet.selectedItem.toString()

                val list = listProduct
                    .filter { it.category == category && it.brand == brand && it.pet == pet }
                    .map { y ->
                        val ff = viewModel.productsAvailability.value?.find { r -> r.productid == y.productId }
                        Availability(
                            y.productId,
                            y.description.toString(),
                            y.brand.toString(),
                            y.competence.toString(),
                            ff != null,
                            ff?.uuid ?: y.uuid
                        )
                    }

                (rv.adapter as AdapterAvailability).updateAvailability(list.toMutableList())
            }
        }

        rv.adapter = AdapterAvailability(ArrayList(), object : AdapterAvailability.ListAdapterListener {
            override fun availability(availability: Availability, position: Int) {
                viewModel.manageProductAvailability(availability, rv.context, pointSale.visitId)
            }
        })
    }
}