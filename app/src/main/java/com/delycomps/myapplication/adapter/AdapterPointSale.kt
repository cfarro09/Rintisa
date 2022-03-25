package com.delycomps.myapplication.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.R
import com.delycomps.myapplication.model.PointSale
import java.lang.Exception

class AdapterPointsale(
    private var listPointSale: List<PointSale>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterPointsale.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listPointSale.size
    }

    interface ListAdapterListener { // create an interface
        fun onClickAtDetailPointSale(pointSale1: PointSale, position: Int)  // create callback function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_point_sale, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateImageBefore(position: Int, url: String){
        listPointSale[position].imageBefore = url
        notifyItemChanged(position)
    }

    fun updateManagement(position: Int, status: String){
        listPointSale[position].management = status
        notifyItemChanged(position)
    }

    fun updateImageAfter(position: Int, url: String){
        listPointSale[position].imageAfter = url
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val pointSale: PointSale = listPointSale[position]

        holder.itemPointSaleClient.text = pointSale.client
        holder.itemPointSaleMarket.text = pointSale.market
        holder.itemPointSaleStallNumber.text = "N° PUESTO: " + pointSale.stallNumber
        holder.itemPointSaleManagement.text = pointSale.management
    }

    inner class OrderViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        internal var itemPointSaleClient: TextView = itemView.findViewById(R.id.pdv_client)
        internal var itemPointSaleMarket: TextView = itemView.findViewById(R.id.pdv_market)
        internal var itemPointSaleStallNumber: TextView = itemView.findViewById(R.id.pdv_stall_number)
        internal var itemPointSaleManagement: TextView = itemView.findViewById(R.id.pdv_management)
//        internal var itemPointSaleDate: TextView = itemView.findViewById(R.id.pdv_date)
//        internal var itemPointSaleLastVisit: TextView = itemView.findViewById(R.id.pdv_last_visit)
        internal var itemPointSaleBackground: RelativeLayout = itemView.findViewById(R.id.pdv_background)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val pointSale = listPointSale[position]
                refListener.onClickAtDetailPointSale(pointSale, position)
            }
        }

    }
}

