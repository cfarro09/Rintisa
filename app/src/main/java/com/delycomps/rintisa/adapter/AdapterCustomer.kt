package com.delycomps.rintisa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.Customer
import java.util.ArrayList

class AdapterCustomer(
    private var listCustomer: List<Customer>,
    private val refListener: ListAdapterListener,
) : RecyclerView.Adapter<AdapterCustomer.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listCustomer.size
    }

    interface ListAdapterListener { // create an interface
        fun onClickAtDetailCustomer(pointSale1: Customer, position: Int)  // create callback function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_customer, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setFilter(list: List<Customer>) {
        listCustomer = ArrayList()
        (listCustomer as ArrayList<Customer>).addAll(list)
        notifyDataSetChanged()
    }

    fun updateStatus(position: Int, status: String){
        listCustomer[position].status = status
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val pointSale: Customer = listCustomer[position]

        holder.itemCustomerClient.text = pointSale.client
        holder.itemCustomerMarket.text = pointSale.market
        holder.itemCustomerStatus.text = pointSale.status
        holder.itemCustomerStallNumber.text = "N° PUESTO: " + (pointSale.stallNumber ?: "")

    }

    inner class OrderViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        internal var itemCustomerClient: TextView = itemView.findViewById(R.id.pdv_client)
        internal var itemCustomerMarket: TextView = itemView.findViewById(R.id.pdv_market)
        internal var itemCustomerStallNumber: TextView = itemView.findViewById(R.id.pdv_stall_number)
        internal var itemCustomerStatus: TextView = itemView.findViewById(R.id.pdv_status)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val pointSale = listCustomer[position]
                refListener.onClickAtDetailCustomer(pointSale, position)
            }
        }

    }
}

