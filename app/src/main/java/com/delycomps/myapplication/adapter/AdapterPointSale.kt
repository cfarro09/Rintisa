package com.delycomps.myapplication.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.R
import com.delycomps.myapplication.model.PointSale
import java.lang.Exception
import java.util.ArrayList

class AdapterPointsale(
    private var listPointSale: List<PointSale>,
    private val refListener: ListAdapterListener,
    private val supervisor: Boolean = false
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

    fun setFilter(list: List<PointSale>) {
        listPointSale = ArrayList()
        (listPointSale as ArrayList<PointSale>).addAll(list)
        notifyDataSetChanged()
    }
    fun updateManagementSup(position: Int, status: String){
        listPointSale[position].managementSup = status
        notifyItemChanged(position)
    }
    fun updateManagement(position: Int, status: String){
        listPointSale[position].management = status
        notifyItemChanged(position)
    }
    fun updateManagement(position: Int, status: String, dateFinish: String, statuslocal: String){
        listPointSale[position].management = status
        listPointSale[position].dateFinish = dateFinish
        listPointSale[position].wasSaveOnBD = statuslocal == "ENVIADO"
        notifyItemChanged(position)
    }

    fun updateDateStart(position: Int, dateStart: String, latitude: Double, longitude: Double){
        listPointSale[position].dateStart = dateStart
        listPointSale[position].latitudeStart = latitude
        listPointSale[position].longitudeStart = longitude
        listPointSale[position].management = "INICIADO"
        notifyItemChanged(position)
    }

    fun updateImageAfter(position: Int, url: String){
        listPointSale[position].imageAfter = url
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val pointSale: PointSale = listPointSale[position]


        holder.itemPointSaleMarket.text = pointSale.market
        holder.itemPointSaleStallNumber.text = "N° PUESTO: " + (pointSale.stallNumber ?: "")

        if (supervisor) {
            holder.itemPointSaleClient.text = pointSale.client + " " + pointSale.management
            holder.itemPointSaleManagement.text = pointSale.managementSup

            holder.itemPointSaleManagement.visibility = View.VISIBLE
            holder.itemPointSaleHourEntry.visibility = View.VISIBLE
            holder.itemPointSaleUsername.text = pointSale.user
            holder.itemPointSaleHourEntry.text = pointSale.hourEntry
        } else {
            holder.itemPointSaleClient.text = pointSale.client

            holder.itemPointSaleManagement.text = pointSale.management

            holder.itemPointSaleUsername.visibility = View.GONE
            holder.itemPointSaleHourEntry.visibility = View.GONE

            if (pointSale.management == "VISITADO" && !pointSale.wasSaveOnBD) {
                holder.background.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ff8f8f"))
            }
        }
    }

    inner class OrderViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        internal var itemPointSaleClient: TextView = itemView.findViewById(R.id.pdv_client)
        internal var itemPointSaleMarket: TextView = itemView.findViewById(R.id.pdv_market)
        internal var itemPointSaleStallNumber: TextView = itemView.findViewById(R.id.pdv_stall_number)
        internal var itemPointSaleManagement: TextView = itemView.findViewById(R.id.pdv_management)
        internal var itemPointSaleUsername: TextView = itemView.findViewById(R.id.pdv_username)
        internal var itemPointSaleHourEntry: TextView = itemView.findViewById(R.id.pdv_hour_entry)
        internal var background: CardView = itemView.findViewById(R.id.pdv_background)

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

