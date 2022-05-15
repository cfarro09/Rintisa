package com.delycomps.myapplication.ui.auditor

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.AuditorViewModel
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.SupervisorViewModel
import com.delycomps.myapplication.adapter.AdapterBins
import com.delycomps.myapplication.adapter.AdapterCheck
import com.delycomps.myapplication.adapter.AdapterPointsale
import com.delycomps.myapplication.cache.Helpers
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.Customer
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val CODE_RESULT_CAMERA = 10001

class BinsFragment : Fragment() {
    private lateinit var viewModel: AuditorViewModel
    private var currentPhotoPath: String = ""
    private var numberImage: String = "0"
    private lateinit var dialogLoading: AlertDialog
    private lateinit var customer: Customer

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
        val spinnerBins = view.findViewById<Spinner>(R.id.spinner_bins)

        spinnerBins.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("NINGUNO") + (viewModel.dataCheckSupPromoter.value?.map { it.decription }?.toMutableList() ?: ArrayList()))

        val rvBins: RecyclerView = view.findViewById(R.id.main_rv_bins)
        rvBins.layoutManager = LinearLayoutManager(view.context)

        rvBins.adapter = AdapterBins(ArrayList())

        spinnerBins.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val valueSelected = spinnerBins.selectedItem.toString()
                val aa = (viewModel.dataCheckSupPromoter.value ?: emptyList()).find { it.decription == valueSelected }
                if (aa != null) {
                    (rvBins.adapter as AdapterBins).addBin(aa)
                }
            }
        }

        button.setOnClickListener {
            val ob = JSONObject()
            ob.put("customerid", customer.customerId)
            val ob1 = JSONObject()

            (rvBins.adapter as AdapterBins).getList().forEach {
                ob1.put(it.key, it.flag)
            }
            ob.put("trash_can", ob1.toString())

            viewModel.executeSupervisor(ob, "UFN_AUDITOR_MANAGEMENT_INS", SharedPrefsCache(view.context).getToken())
        }

        viewModel.resExecute.observe(requireActivity()) {
            if (it.result == "UFN_AUDITOR_MANAGEMENT_INS") {
                if (!it.loading && it.success) {
//                    dialogLoading.dismiss()
                    viewModel.initExecute()
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