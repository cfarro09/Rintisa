package com.delycomps.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.R
import com.delycomps.myapplication.model.Availability
import com.google.android.material.switchmaterial.SwitchMaterial

class AdapterAvailability(
    private var availabilityList: MutableList<Availability>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterAvailability.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return availabilityList.size
    }

    interface ListAdapterListener { // create an interface
        fun availability(availability: Availability, position: Int)  // create callback function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_availability, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateAvailability(availabilityList1: MutableList<Availability>) {
        availabilityList = availabilityList1
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val availability: Availability = availabilityList[position]

        holder.availabilityName.text = availability.description
        holder.availabilityFlag.isChecked = availability.flag ?: false
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var availabilityFlag: SwitchMaterial = itemView.findViewById(R.id.product_availability_flag)
        internal var availabilityName: TextView = itemView.findViewById(R.id.product_availability_name)

        init {
            availabilityFlag.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val availability = availabilityList[position]
                    availability.flag = isChecked
                    refListener.availability(availability, position)
                }
            }
        }
    }
}

