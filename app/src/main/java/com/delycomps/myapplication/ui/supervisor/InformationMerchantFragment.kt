package com.delycomps.myapplication.ui.supervisor

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.delycomps.myapplication.Constants
import com.delycomps.myapplication.R
import com.delycomps.myapplication.model.PointSale


class InformationMerchant : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_information, container, false)
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