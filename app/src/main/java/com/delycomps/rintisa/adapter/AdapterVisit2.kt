package com.delycomps.rintisa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.Visit2
import com.google.android.material.switchmaterial.SwitchMaterial

class AdapterVisit2(
    private var listVisit2: List<Visit2>
) : RecyclerView.Adapter<AdapterVisit2.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listVisit2.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_visit2, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val visit2: Visit2 = listVisit2[position]

        holder.status.text = visit2.status
        holder.customer.text = visit2.customer
        holder.market.text = visit2.market
        holder.semaphore.text = visit2.semaphore
        holder.visitStart.text = visit2.visitStart
        holder.visitFinish.text = visit2.visitFinish
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var status: TextView = itemView.findViewById(R.id.visit2_status)
        var customer: TextView = itemView.findViewById(R.id.visit2_customer)
        var market: TextView = itemView.findViewById(R.id.visit2_market)
        var semaphore: TextView = itemView.findViewById(R.id.visit2_semaphore)
        var visitStart: TextView = itemView.findViewById(R.id.visit2_visit_start)
        var visitFinish: TextView = itemView.findViewById(R.id.visit2_visit_finish)
    }
}

