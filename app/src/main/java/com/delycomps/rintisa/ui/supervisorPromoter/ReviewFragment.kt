package com.delycomps.rintisa.ui.supervisorPromoter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.SupervisorViewModel
import com.delycomps.rintisa.adapter.AdapterCheck
import com.delycomps.rintisa.model.CheckSupPromoter
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

//        viewModel.resExecute.observe(requireActivity()) {
//            if (it.result == "QUERY_UPDATE_JSON_UNIFORM1" || it.result == "QUERY_UPDATE_JSON_MATERIALS1") {
//                if (!it.loading && it.success) {
////                    dialogLoading.dismiss()
//                    viewModel.initExecute()
//                    val text = if (it.result == "QUERY_UPDATE_JSON_UNIFORM") "Uniforme actualizado correctamente" else "Materiales actualizado correctamente"
//                    Toast.makeText(view.context, text, Toast.LENGTH_LONG).show()
//                } else if (!it.loading && !it.success) {
////                    dialogLoading.dismiss()
//                    viewModel.initExecute()
//                    Toast.makeText(view.context, "Ocurrió un error inesperado", Toast.LENGTH_LONG).show()
//                } else if (it.loading) {
////                    dialogLoading.show()
//                }
//            }
//        }

        val rvUniform: RecyclerView = view.findViewById(R.id.main_rv_uniform)
        rvUniform.layoutManager = LinearLayoutManager(view.context)

        val rvMaterial: RecyclerView = view.findViewById(R.id.main_rv_materials)
        rvMaterial.layoutManager = LinearLayoutManager(view.context)

        rvUniform.adapter = AdapterCheck((viewModel.dataCheckSupPromoter.value?.filter { it.type == "UNIFORM" }?.toMutableList() ?: ArrayList()), object : AdapterCheck.ListAdapterListener {
            override fun updateList(qList: List<CheckSupPromoter>) {
                val ob1 = JSONObject()
                qList.forEach { ob1.put(it.key, it.flag) }
                viewModel.setUniform(ob1.toString())
            }
        })

        rvMaterial.adapter = AdapterCheck((viewModel.dataCheckSupPromoter.value?.filter { it.type == "MATERIAL" }?.toMutableList() ?: ArrayList()), object : AdapterCheck.ListAdapterListener {
            override fun updateList(qList: List<CheckSupPromoter>) {
                val ob1 = JSONObject()
                qList.forEach { ob1.put(it.key, it.flag) }
                viewModel.setMaterial(ob1.toString())
            }
        })
    }
}