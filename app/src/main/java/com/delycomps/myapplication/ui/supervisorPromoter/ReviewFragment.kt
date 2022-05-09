package com.delycomps.myapplication.ui.supervisorPromoter

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.SupervisorViewModel
import com.delycomps.myapplication.adapter.AdapterCheck
import com.delycomps.myapplication.adapter.AdapterQuestions
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.model.Question
import com.google.gson.Gson
import org.json.JSONObject

class ReviewFragment : Fragment() {
    private lateinit var viewModel: SupervisorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_review, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(SupervisorViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        val dialogLoading: AlertDialog = builderLoading.create()

        viewModel.resExecute.observe(requireActivity()) {
            if (it.result == "QUERY_UPDATE_JSON_UNIFORM" || it.result == "QUERY_UPDATE_JSON_MATERIALS") {
                if (!it.loading && it.success) {
//                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    val text = if (it.result == "QUERY_UPDATE_JSON_UNIFORM") "Uniforme actualizado correctamente" else "Materiales actualizado correctamente"
                    Toast.makeText(view.context, text, Toast.LENGTH_LONG).show()
                } else if (!it.loading && !it.success) {
//                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    Toast.makeText(view.context, "Ocurrió un error inesperado", Toast.LENGTH_LONG).show()
                } else if (it.loading) {
//                    dialogLoading.show()
                }
            }
        }

        val buttonSaveUniform: ImageButton = view.findViewById(R.id.button_save_uniform)
        val buttonSaveMaterials: ImageButton = view.findViewById(R.id.button_save_materials)

        val rvUniform: RecyclerView = view.findViewById(R.id.main_rv_uniform)
        rvUniform.layoutManager = LinearLayoutManager(view.context)

        val rvMaterial: RecyclerView = view.findViewById(R.id.main_rv_materials)
        rvMaterial.layoutManager = LinearLayoutManager(view.context)

        val pointSale: PointSale? = activity?.intent?.getParcelableExtra(Constants.POINT_SALE_ITEM)

        rvUniform.adapter = AdapterCheck((viewModel.dataCheckSupPromoter.value?.filter { it.type == "UNIFORM" }?.toMutableList() ?: ArrayList()))
        rvMaterial.adapter = AdapterCheck((viewModel.dataCheckSupPromoter.value?.filter { it.type == "MATERIAL" }?.toMutableList() ?: ArrayList()))

        buttonSaveUniform.setOnClickListener {
            val ob = JSONObject()
            ob.put("customerid", pointSale?.customerId)
            val ob1 = JSONObject()
            (rvUniform.adapter as AdapterCheck).getList().forEach {
                ob1.put(it.key, it.flag)
            }
            ob.put("json", ob1.toString())

            viewModel.executeSupervisor(ob, "QUERY_UPDATE_JSON_UNIFORM", SharedPrefsCache(view.context).getToken())
        }

        buttonSaveMaterials.setOnClickListener {
            val ob = JSONObject()
            ob.put("customerid", pointSale?.customerId)
            val ob1 = JSONObject()
            (rvMaterial.adapter as AdapterCheck).getList().forEach {
                ob1.put(it.key, it.flag)
            }
            ob.put("json", ob1.toString())

            viewModel.executeSupervisor(ob, "QUERY_UPDATE_JSON_MATERIALS", SharedPrefsCache(view.context).getToken())
        }
    }
}