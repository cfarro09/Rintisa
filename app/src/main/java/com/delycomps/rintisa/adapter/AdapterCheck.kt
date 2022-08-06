package com.delycomps.rintisa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.Availability
import com.delycomps.rintisa.model.CheckSupPromoter
import com.google.android.material.switchmaterial.SwitchMaterial

class AdapterCheck(
    private var questionList: MutableList<CheckSupPromoter>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterCheck.OrderViewHolder>() {
    private lateinit var mContext: Context

    interface ListAdapterListener { // create an interface
        fun updateList(qList: List<CheckSupPromoter>)  // create callback function
    }

    override fun getItemCount(): Int {
        return questionList.size
    }

    fun getList() : List<CheckSupPromoter> {
        return questionList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_availability, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val availability: CheckSupPromoter = questionList[position]

        holder.availabilityName.text = availability.decription
        holder.availabilityFlag.isChecked = availability.flag
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var availabilityFlag: SwitchMaterial = itemView.findViewById(R.id.product_availability_flag)
        internal var availabilityName: TextView = itemView.findViewById(R.id.product_availability_name)

        init {
            availabilityFlag.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    questionList[position].flag = isChecked
                    refListener.updateList(questionList)
                }
            }
        }
    }
}

