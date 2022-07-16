package com.delycomps.rintisa.ui.supervisor

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.Constants
import com.delycomps.rintisa.R
import com.delycomps.rintisa.SupervisorViewModel
import com.delycomps.rintisa.adapter.AdapterQuestions
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.model.PointSale
import com.delycomps.rintisa.model.Question
import com.google.gson.Gson
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AuditFragment : Fragment() {
    private lateinit var viewModel: SupervisorViewModel
    private lateinit var pointSale: PointSale
    private lateinit var dialogLoading: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audit, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(SupervisorViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button: ImageButton = view.findViewById(R.id.button_save)
        val rv: RecyclerView = view.findViewById(R.id.rv_question)
        rv.layoutManager = LinearLayoutManager(view.context)
        pointSale = requireActivity().intent.getParcelableExtra(Constants.POINT_SALE_ITEM)!!

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        dialogLoading = builderLoading.create()

        rv.adapter = AdapterQuestions(viewModel.dataQuestion.value?.toMutableList() ?: ArrayList(), object : AdapterQuestions.ListAdapterListener {
            override fun availability(question: Question, position: Int) {
                viewModel.manageQuestion(question, rv.context, 0)
            }
        })

        viewModel.resExecute.observe(requireActivity()) {
            if ((it.result ?: "") == "UFN_AUDIT_DETAIL_INS") {
                if (!it.loading && it.success) {
                    dialogLoading.dismiss()
                    (rv.adapter as AdapterQuestions).updateQuestion(viewModel.dataQuestion.value?.toMutableList() ?: ArrayList())
                    Toast.makeText(view.context, "Auditoria registrada correctamente", Toast.LENGTH_LONG).show()
                    viewModel.initExecute()
                } else if (!it.loading && !it.success) {
                    dialogLoading.dismiss()
                    viewModel.initExecute()
                    Toast.makeText(view.context, "Ocurrió un error inesperado", Toast.LENGTH_LONG).show()
                }
            }
        }

        button.setOnClickListener {
            val json = Gson().toJson((rv.adapter as AdapterQuestions).getList().map { mapOf<String, Any>(
                "description_audit" to SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US).format(Date()),
                "type_audit" to "NINGUNO",
                "question" to it.text,
                "flag" to it.flag,
                "score" to it.score,
                "description_auditdetail" to it.text,
                "type_auditdetail" to "NINGUNO",
            )})
            val ob = JSONObject()
            ob.put("auditdetail", json)
            ob.put("customerid", pointSale.customerId)
            ob.put("visitid", pointSale.visitId)

            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialogLoading.show()
                        viewModel.executeSupervisor(ob, "UFN_AUDIT_DETAIL_INS", SharedPrefsCache(view.context).getToken())
                    }
                }
            }
            val builder = AlertDialog.Builder(view.context)
            builder.setMessage("¿Está seguro de enviar la auditoria??")
                .setPositiveButton(Html.fromHtml("<b>Continuar<b>"), dialogClickListener)
                .setNegativeButton(Html.fromHtml("<b>Cancelar<b>"), dialogClickListener)
            val alert = builder.create()
            alert.show()

        }
    }
}