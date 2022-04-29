package com.delycomps.myapplication.ui.supervisor

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.SupervisorViewModel
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.PointSale
import org.json.JSONObject


class InformationMerchant : Fragment() {
    private lateinit var viewModel: SupervisorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_merchant_information, container, false)
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(requireActivity()).get(SupervisorViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pointSale: PointSale? = activity?.intent?.getParcelableExtra(Constants.POINT_SALE_ITEM)
        if (pointSale != null) {
            view.findViewById<TextView>(R.id.pdv_client).text = pointSale.client
            view.findViewById<TextView>(R.id.pdv_market).text = pointSale.market
            view.findViewById<TextView>(R.id.pdv_stall_number).text = "N° PUESTO: " + pointSale.stallNumber
            view.findViewById<TextView>(R.id.pdv_date).text = "${pointSale.visitFrequency} (${pointSale.visitDay})"
            view.findViewById<TextView>(R.id.pdv_last_visit).text = pointSale.lastVisit
            view.findViewById<TextView>(R.id.pdv_motive).text = pointSale.management + " - " + pointSale.motive

            val button = view.findViewById<ImageButton>(R.id.button_save)
            val editComment = view.findViewById<EditText>(R.id.text_comment)

            button.setOnClickListener {
                val comment = editComment.text.toString()

                if (comment != "") {
                    val ob = JSONObject()
                    ob.put("customerid", pointSale.customerId)
                    ob.put("comment", comment)

                    viewModel.executeSupervisor(ob, "QUERY_UPDATE_COMMENT", SharedPrefsCache(view.context).getToken())
                }
            }

            viewModel.resExecute.observe(requireActivity()) {
                if ((it.result ?: "") == "QUERY_UPDATE_COMMENT") {
                    if (!it.loading && it.success) {
                        viewModel.initExecute()
                        Toast.makeText(view.context, "Comentario registrado correctamente", Toast.LENGTH_LONG).show()
                    } else if (!it.loading && !it.success) {
                        viewModel.initExecute()
                        Toast.makeText(view.context, "Ocurrió un error inesperado", Toast.LENGTH_LONG).show()
                    }
                }
            }

            val color = if (pointSale.trafficLights == "AMARILLO") "#FFF8B7" else if (pointSale.trafficLights == "VERDE") "#B6FFA9" else  "#FF9696"
            view.findViewById<View>(R.id.pdv_traffic_light).backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(color))

            Glide.with(this)
                .load(pointSale.imageBefore)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.loadingtmp)
                .into(view.findViewById(R.id.view_image_before))

            Glide.with(this)
                .load(pointSale.imageAfter)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.loadingtmp)
                .into(view.findViewById(R.id.view_image_after))
        }
    }
}