package com.delycomps.rintisa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.VisitSupervisor
import java.util.ArrayList

class AdapterVisitPending(
    private var listVisitSupervisor: List<VisitSupervisor>,
    private val refListener: ListAdapterListener,
    private val supervisor: Boolean = false
) : RecyclerView.Adapter<AdapterVisitPending.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listVisitSupervisor.size
    }

    interface ListAdapterListener { // create an interface
        fun onClickAtDetailVisitSupervisor(pointSale1: VisitSupervisor, position: Int)  // create callback function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_visit_pending, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val pointSale: VisitSupervisor = listVisitSupervisor[position]

        holder.itemVisitSupervisor.text = pointSale.customer
        holder.itemService.text = pointSale.type
    }

    inner class OrderViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        internal var itemVisitSupervisor: TextView = itemView.findViewById(R.id.supervisor_customer)
        internal var itemService: TextView = itemView.findViewById(R.id.supervisor_service)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val pointSale = listVisitSupervisor[position]
                refListener.onClickAtDetailVisitSupervisor(pointSale, position)
            }
        }

    }
}

