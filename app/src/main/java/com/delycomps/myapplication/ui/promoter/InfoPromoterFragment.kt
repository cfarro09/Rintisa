package com.delycomps.myapplication.ui.promoter

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.model.PointSale

/**
 * A placeholder fragment containing a simple view.
 */
class InfoPromoterFragment : Fragment() {

    private lateinit var viewModel: PromoterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_promoter, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PromoterViewModel::class.java)
        // TODO: Use the ViewModel
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

            val color = if (pointSale.trafficLights == "AMARILLO") "#FFF8B7" else if (pointSale.trafficLights == "VERDE") "#B6FFA9" else  "#FF9696"
            view.findViewById<View>(R.id.pdv_traffic_light).backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(color))
        }
    }
}