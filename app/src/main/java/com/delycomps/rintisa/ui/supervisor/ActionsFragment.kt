package com.delycomps.rintisa.ui.supervisor

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.delycomps.rintisa.Constants
import com.delycomps.rintisa.R
import com.delycomps.rintisa.SupervisorViewModel
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.model.*
import org.json.JSONObject

class ActionsFragment : Fragment() {
    private lateinit var viewModel: SupervisorViewModel
    private val actions = listOf("PROMOCIONES AL CLIENTE FINAL", "PROMOCIONES AL TENDERO", "MATERIAL POP", "NUEVAS MARCAS")
    private lateinit var pointSale: PointSale
    private lateinit var dialogLoading: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_actions, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(SupervisorViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pointSale = requireActivity().intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        val spinnerActions = view.findViewById<Spinner>(R.id.spinner_action)
        val saveAction = view.findViewById<Button>(R.id.save_action)

        val editCity = view.findViewById<EditText>(R.id.text_city)
        val editDistrict = view.findViewById<EditText>(R.id.text_district)
        val editMarket = view.findViewById<EditText>(R.id.text_market)
        val editCompany = view.findViewById<EditText>(R.id.text_company)
        val editBrand = view.findViewById<EditText>(R.id.text_brand)
        val editActivityDirection = view.findViewById<EditText>(R.id.text_activity_direction)
        val editActionDescription = view.findViewById<EditText>(R.id.text_action_description)
        val editMaterial = view.findViewById<EditText>(R.id.text_material)
        val editKgPrice = view.findViewById<EditText>(R.id.text_kg_price)
        val editSacoPrice = view.findViewById<EditText>(R.id.text_saco_price)

        val containerCity = view.findViewById<LinearLayout>(R.id.container_city)
        val containerDistrict = view.findViewById<LinearLayout>(R.id.container_district)
        val containerMarket = view.findViewById<LinearLayout>(R.id.container_market)
        val containerCompany = view.findViewById<LinearLayout>(R.id.container_company)
        val containerBrand = view.findViewById<LinearLayout>(R.id.container_brand)
        val containerActivityDirection = view.findViewById<LinearLayout>(R.id.container_activity_direction)
        val containerActionDescription = view.findViewById<LinearLayout>(R.id.container_action_description)
        val containerMaterial = view.findViewById<LinearLayout>(R.id.container_material)
        val containerKgPrice = view.findViewById<LinearLayout>(R.id.container_kg_price)
        val containerSacoPrice = view.findViewById<LinearLayout>(R.id.container_saco_price)

        spinnerActions.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, actions)

        viewModel.resExecute.observe(requireActivity()) {
            if ((it.result ?: "") == "UFN_SUPERVISOR_HISTORY_INS") {
                if (!it.loading && it.success) {
                    dialogLoading.dismiss()
                    Toast.makeText(view.context, "Acción registrada correctamente", Toast.LENGTH_LONG).show()
                    viewModel.initExecute()
                    editCity.text = Editable.Factory.getInstance().newEditable("")
                    editDistrict.text = Editable.Factory.getInstance().newEditable("")
                    editMarket.text = Editable.Factory.getInstance().newEditable("")
                    editCompany.text = Editable.Factory.getInstance().newEditable("")
                    editBrand.text = Editable.Factory.getInstance().newEditable("")
                    editActivityDirection.text = Editable.Factory.getInstance().newEditable("")
                    editActionDescription.text = Editable.Factory.getInstance().newEditable("")
                    editMaterial.text = Editable.Factory.getInstance().newEditable("")
                    editKgPrice.text = Editable.Factory.getInstance().newEditable("")
                    editSacoPrice.text = Editable.Factory.getInstance().newEditable("")
                } else if (!it.loading && !it.success) {
                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    Toast.makeText(view.context, "Ocurrió un error inesperado", Toast.LENGTH_LONG).show()
                }
            }
        }

        spinnerActions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                containerCity.visibility = View.GONE
                containerDistrict.visibility = View.GONE
                containerMarket.visibility = View.GONE
                containerCompany.visibility = View.GONE
                containerBrand.visibility = View.GONE
                containerActivityDirection.visibility = View.GONE
                containerActionDescription.visibility = View.GONE
                containerMaterial.visibility = View.GONE
                containerKgPrice.visibility = View.GONE
                containerSacoPrice.visibility = View.GONE

                val value = spinnerActions.selectedItem.toString()
                when (value) {
                    "PROMOCIONES AL CLIENTE FINAL" -> {
                        containerCity.visibility = View.VISIBLE
                        containerDistrict.visibility = View.VISIBLE
                        containerMarket.visibility = View.VISIBLE
                        containerCompany.visibility = View.VISIBLE
                        containerBrand.visibility = View.VISIBLE
                        containerActivityDirection.visibility = View.VISIBLE
                        containerActionDescription.visibility = View.VISIBLE
                    }
                    "PROMOCIONES AL TENDERO" -> {
                        containerCity.visibility = View.VISIBLE
                        containerDistrict.visibility = View.VISIBLE
                        containerMarket.visibility = View.VISIBLE
                        containerCompany.visibility = View.VISIBLE
                        containerBrand.visibility = View.VISIBLE
                        containerActionDescription.visibility = View.VISIBLE
                    }
                    "MATERIAL POP" -> {
                        containerCity.visibility = View.VISIBLE
                        containerDistrict.visibility = View.VISIBLE
                        containerMarket.visibility = View.VISIBLE
                        containerCompany.visibility = View.VISIBLE
                        containerBrand.visibility = View.VISIBLE
                        containerMaterial.visibility = View.VISIBLE
                        containerActionDescription.visibility = View.VISIBLE
                    }
                    "NUEVAS MARCAS" -> {
                        containerCity.visibility = View.VISIBLE
                        containerDistrict.visibility = View.VISIBLE
                        containerMarket.visibility = View.VISIBLE
                        containerCompany.visibility = View.VISIBLE
                        containerBrand.visibility = View.VISIBLE
                        containerKgPrice.visibility = View.VISIBLE
                        containerSacoPrice.visibility = View.VISIBLE
                    }
                }
            }
        }

        saveAction.setOnClickListener {
            var error = false
            val ob = JSONObject()
            ob.put("city", "")
            ob.put("type", spinnerActions.selectedItem.toString())
            ob.put("district", "")
            ob.put("market", "")
            ob.put("company", "")
            ob.put("brand", "")
            ob.put("activity_direction", "")
            ob.put("action_description", "")
            ob.put("material", "")
            ob.put("kg_price", 0)
            ob.put("saco_price", 0)
            ob.put("description", "")
            ob.put("type", "NINGUNA")
            ob.put("customerid", pointSale.customerId)
            ob.put("visitid", pointSale.visitId)

            when (spinnerActions.selectedItem.toString()) {
                "PROMOCIONES AL CLIENTE FINAL" -> {
                    val valueCity = editCity.text.toString()
                    val valueDistrict = editDistrict.text.toString()
                    val valueMarket = editMarket.text.toString()
                    val valueCompany = editCompany.text.toString()
                    val valueBrand = editBrand.text.toString()
                    val valueActivityDirection = editActivityDirection.text.toString()
                    val valueActionDescription = editActionDescription.text.toString()

                    if (valueCity == "" || valueDistrict == "" || valueMarket == "" || valueCompany == "" || valueBrand == "" || valueActivityDirection == "" || valueActionDescription == "") {
                        Toast.makeText(view.context, "Debe completar todos los campos", Toast.LENGTH_LONG).show()
                        error = true
                    } else {
                        ob.put("city", valueCity)
                        ob.put("district", valueDistrict)
                        ob.put("market", valueMarket)
                        ob.put("company", valueCompany)
                        ob.put("brand", valueBrand)
                        ob.put("activity_direction", valueActivityDirection)
                        ob.put("action_description", valueActionDescription)
                    }
                }
                "PROMOCIONES AL TENDERO" -> {
                    val valueCity = editCity.text.toString()
                    val valueDistrict = editDistrict.text.toString()
                    val valueMarket = editMarket.text.toString()
                    val valueCompany = editCompany.text.toString()
                    val valueBrand = editBrand.text.toString()
                    val valueActionDescription = editActionDescription.text.toString()

                    if (valueCity == "" || valueDistrict == "" || valueMarket == "" || valueCompany == "" || valueBrand == "" || valueActionDescription == "") {
                        Toast.makeText(view.context, "Debe completar todos los campos", Toast.LENGTH_LONG).show()
                        error = true
                    } else {
                        ob.put("city", valueCity)
                        ob.put("district", valueDistrict)
                        ob.put("market", valueMarket)
                        ob.put("company", valueCompany)
                        ob.put("brand", valueBrand)
                        ob.put("action_description", valueActionDescription)
                    }
                }
                "MATERIAL POP" -> {
                    val valueCity = editCity.text.toString()
                    val valueDistrict = editDistrict.text.toString()
                    val valueMarket = editMarket.text.toString()
                    val valueCompany = editCompany.text.toString()
                    val valueBrand = editBrand.text.toString()
                    val valueMaterial = editMaterial.text.toString()
                    val valueActionDescription = editActionDescription.text.toString()

                    if (valueCity == "" || valueDistrict == "" || valueMarket == "" || valueCompany == "" || valueBrand == "" || valueMaterial == "" || valueActionDescription == "") {
                        Toast.makeText(view.context, "Debe completar todos los campos", Toast.LENGTH_LONG).show()
                        error = true
                    } else {
                        ob.put("city" , valueCity)
                        ob.put("district" , valueDistrict)
                        ob.put("market" , valueMarket)
                        ob.put("company" , valueCompany)
                        ob.put("brand" , valueBrand)
                        ob.put("material" , valueMaterial)
                        ob.put("action_description" , valueActionDescription)
                    }
                }
                "NUEVAS MARCAS" -> {
                    val valueCity = editCity.text.toString()
                    val valueDistrict = editDistrict.text.toString()
                    val valueMarket = editMarket.text.toString()
                    val valueCompany = editCompany.text.toString()
                    val valueBrand = editBrand.text.toString()
                    val valueKgPrice = editKgPrice.text.toString()
                    val valueSacoPrice = editSacoPrice.text.toString()

                    if (valueCity == "" || valueDistrict == "" ||valueMarket == "" ||valueCompany == "" ||valueBrand == "" ||valueKgPrice == "" ||valueSacoPrice == "") {
                        Toast.makeText(view.context, "Debe completar todos los campos", Toast.LENGTH_LONG).show()
                        error = true
                    } else {
                        ob.put("city", valueCity)
                        ob.put("district", valueDistrict)
                        ob.put("market", valueMarket)
                        ob.put("company", valueCompany)
                        ob.put("brand", valueBrand)
                        ob.put("kg_price", if (valueKgPrice == "") 0 else valueKgPrice.toDouble())
                        ob.put("saco_price", if (valueSacoPrice == "") 0 else valueSacoPrice.toDouble())
                    }
                }
            }

            if (!error) {
                dialogLoading.show()
                viewModel.executeSupervisor(ob, "UFN_SUPERVISOR_HISTORY_INS", SharedPrefsCache(view.context).getToken())
            }
        }
    }
}