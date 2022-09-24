package com.delycomps.rintisa

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.adapter.AdapterQuestionDynamic
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.model.Customer
import org.json.JSONObject

class AuditorClientFragment : Fragment() {
    private lateinit var viewModel: AuditorViewModel
    private lateinit var dialogLoading: AlertDialog
    private lateinit var customer: Customer
    private var status2 = "EN ESPERA"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_auditor_client, container, false)
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

        val listQuestions = viewModel.questionClients.value ?: arrayListOf()
        var lasttype = ""
        for (ii in listQuestions.indices) {
            val tt = listQuestions[ii].type
            if (lasttype == "") {
                lasttype = listQuestions[ii].type
            } else {
                if (lasttype == listQuestions[ii].type) {
                    listQuestions[ii].type = ""
                }
                lasttype = tt
            }
        }
        rvBins.adapter = AdapterQuestionDynamic(listQuestions.toMutableList())

        button.setOnClickListener {
            val ob = JSONObject()
            ob.put("customerid", customer.customerId)
            val ob1 = JSONObject()

            (rvBins.adapter as AdapterQuestionDynamic).getList().forEach {
                val key = it.key.split("||")[0]
                val type = it.key.split("||")[1]
                if (type == "switch") {
                    ob1.put(key, if (it.flag) "SI" else "NO")
                } else {
                    ob1.put(key, it.value)
                }
            }
            ob.put("json_content", ob1.toString())

            viewModel.executeSupervisor(ob, "UFN_AUDITION_RINTISA_CLIENT_INS", SharedPrefsCache(view.context).getToken())
        }

        viewModel.loading.observe(requireActivity()) {
            if (it) {
                dialogLoading.show()
            } else {
                dialogLoading.dismiss()
            }
        }

        viewModel.resExecute.observe(requireActivity()) {
            if (it.result == "UFN_AUDITION_RINTISA_CLIENT_INS") {
                if (!it.loading && it.success) {
//                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    status2 = "GESTIONADO"
                    val text = "Se insertó correctamente"
                    Toast.makeText(view.context, text, Toast.LENGTH_LONG).show()

                    val output = Intent()
                    output.putExtra("status", "GESTIONADO")
                    requireActivity().setResult(AppCompatActivity.RESULT_OK, output);
                    requireActivity().finish()

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