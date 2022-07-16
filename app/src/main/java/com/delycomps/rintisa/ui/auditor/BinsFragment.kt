package com.delycomps.rintisa.ui.auditor

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.AuditorViewModel
import com.delycomps.rintisa.Constants
import com.delycomps.rintisa.R
import com.delycomps.rintisa.adapter.AdapterBins
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.model.Customer
import org.json.JSONObject
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

private const val CODE_RESULT_CAMERA = 10001

class BinsFragment : Fragment() {
    private lateinit var viewModel: AuditorViewModel
    private var currentPhotoPath: String = ""
    private var numberImage: String = "0"
    private lateinit var dialogLoading: AlertDialog
    private lateinit var customer: Customer
    private var status2 = "EN ESPERA"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bins, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(AuditorViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        customer = activity?.intent?.getParcelableExtra(Constants.POINT_CUSTOMER)!!

        val button = view.findViewById<ImageButton>(R.id.button_save)

        val rvBins: RecyclerView = view.findViewById(R.id.main_rv_bins)
        rvBins.layoutManager = LinearLayoutManager(view.context)

        rvBins.adapter = AdapterBins(viewModel.dataCheckSupPromoter.value?.toMutableList() ?: ArrayList())


        button.setOnClickListener {
            val ob = JSONObject()
            ob.put("customerid", customer.customerId)
            val ob1 = JSONObject()

            (rvBins.adapter as AdapterBins).getList().forEach {
                ob1.put(it.key, (if (it.value == "") "0" else it.value)?.toInt() ?: 0)
            }
            ob.put("trash_can", ob1.toString())

            viewModel.executeSupervisor(ob, "UFN_AUDITOR_MANAGEMENT_INS", SharedPrefsCache(view.context).getToken())
        }

        viewModel.resExecute.observe(requireActivity()) {
            if (it.result == "UFN_AUDITOR_MANAGEMENT_INS") {
                if (!it.loading && it.success) {
//                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    status2 = "GESTIONADO"
                    val text = "Se insertó correctamente"
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
    }



}