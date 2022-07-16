package com.delycomps.rintisa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.Merchandise
import com.google.android.material.switchmaterial.SwitchMaterial

class AdapterMerchandise(
    private var listMerchandise: MutableList<Merchandise>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterMerchandise.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listMerchandise.size
    }

    interface ListAdapterListener { // create an interface
        fun onUpdateMerchandise(merchandise: Merchandise, position: Int)  // create callback function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_merchandise, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val merchandise: Merchandise = listMerchandise[position]

        holder.itemMerchandiseName.text = merchandise.description
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var itemMerchandiseFlag: SwitchMaterial = itemView.findViewById(R.id.merchandise_flag)
        internal var itemMerchandiseName: TextView = itemView.findViewById(R.id.merchandise_name)

        init {
            itemMerchandiseFlag.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val merchandise = listMerchandise[position]
                    merchandise.flag = isChecked
                    refListener.onUpdateMerchandise(merchandise, position)
                }
            }
        }
    }
}

