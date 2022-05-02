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
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.PointSale
import org.json.JSONObject

class StatusFragment : Fragment() {
    private lateinit var viewModel: SupervisorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_status, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(SupervisorViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        val dialogLoading: AlertDialog = builderLoading.create()

        val pointSale: PointSale? = activity?.intent?.getParcelableExtra(Constants.POINT_SALE_ITEM)

        viewModel.resExecute.observe(requireActivity()) {
            if (it.result == "QUERY_UPDATE_JSON_STATUS") {
                if (!it.loading && it.success) {
                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    val text = "Se actualizó correctmente (ESTADO PDV)"
                    Toast.makeText(view.context, text, Toast.LENGTH_LONG).show()
                } else if (!it.loading && !it.success) {
                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    Toast.makeText(view.context, "Ocurrió un error inesperado", Toast.LENGTH_LONG).show()
                } else if (it.loading) {
                    dialogLoading.show()
                }
            }
        }

        val buttonSaveStatus: ImageButton = view.findViewById(R.id.button_save_status)

        val rvStatus: RecyclerView = view.findViewById(R.id.main_rv_status)
        rvStatus.layoutManager = LinearLayoutManager(view.context)

        rvStatus.adapter = AdapterCheck((viewModel.dataCheckSupPromoter.value?.filter { it.type == "PDV" }?.toMutableList() ?: ArrayList()))

        buttonSaveStatus.setOnClickListener {
            val ob = JSONObject()
            ob.put("customerid", pointSale?.customerId)
            val ob1 = JSONObject()
            (rvStatus.adapter as AdapterCheck).getList().forEach {
                ob1.put(it.key, it.flag)
            }
            ob.put("json", ob1.toString())

            viewModel.executeSupervisor(ob, "QUERY_UPDATE_JSON_STATUS", SharedPrefsCache(view.context).getToken())
        }
    }
}