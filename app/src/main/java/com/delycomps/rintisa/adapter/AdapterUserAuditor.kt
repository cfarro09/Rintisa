package com.delycomps.rintisa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.UserFromAuditor
import java.util.ArrayList

class AdapterUserAuditor(
    private var listUserFromAuditor: List<UserFromAuditor>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterUserAuditor.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listUserFromAuditor.size
    }

    interface ListAdapterListener { // create an interface
        fun onClickAtDetailUserFromAuditor(pointSale1: UserFromAuditor, position: Int)  // create callback function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_userauditor, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setFilter(list: List<UserFromAuditor>) {
        listUserFromAuditor = ArrayList()
        (listUserFromAuditor as ArrayList<UserFromAuditor>).addAll(list)
        notifyDataSetChanged()
    }

    fun updateStatus(position: Int, status: String){
        listUserFromAuditor[position].status = status
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val pointSale: UserFromAuditor = listUserFromAuditor[position]

        holder.itemUserStatus.text = pointSale.status
        holder.itemUserDescription.text = pointSale.description
        holder.itemUserInfo.text = "DNI: " + pointSale.docnum + " - TEL: " + pointSale.phone
        holder.itemUserSector.text = pointSale.sector
    }

    inner class OrderViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        internal var itemUserStatus: TextView = itemView.findViewById(R.id.userauditor_status)
        internal var itemUserDescription: TextView = itemView.findViewById(R.id.userauditor_description)
        internal var itemUserInfo: TextView = itemView.findViewById(R.id.userauditor_info)
        internal var itemUserSector: TextView = itemView.findViewById(R.id.userauditor_sector)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val pointSale = listUserFromAuditor[position]
                refListener.onClickAtDetailUserFromAuditor(pointSale, position)
            }
        }

    }
}

