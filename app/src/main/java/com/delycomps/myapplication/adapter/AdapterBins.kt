package com.delycomps.myapplication.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.R
import com.delycomps.myapplication.model.Availability
import com.delycomps.myapplication.model.CheckSupPromoter
import com.delycomps.myapplication.model.Material
import com.delycomps.myapplication.model.Question
import com.google.android.material.switchmaterial.SwitchMaterial

class AdapterBins(
    private var questionList: MutableList<CheckSupPromoter>
) : RecyclerView.Adapter<AdapterBins.OrderViewHolder>() {
    private lateinit var mContext: Context


    override fun getItemCount(): Int {
        return questionList.size
    }

    fun getList() : List<CheckSupPromoter> {
        return questionList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_bins, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun addBin(bin: CheckSupPromoter) {
        val existsBin = questionList.find { it.key == bin.key }
        if (existsBin == null && questionList.count() < 3) {
            questionList.add(bin)
            notifyItemInserted(questionList.count() - 1)
        }
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val availability: CheckSupPromoter = questionList[position]

        holder.availabilityName.text = availability.decription
//        holder.availabilityFlag.isChecked = availability.flag
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        internal var availabilityFlag: SwitchMaterial = itemView.findViewById(R.id.product_availability_flag)
        internal var availabilityName: TextView = itemView.findViewById(R.id.product_availability_name)
        private var textValue: TextView = itemView.findViewById(R.id.item_value)

        init {
            textValue.addTextChangedListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    questionList[position].value = textValue.text.toString()
                }
            }
        }
    }
}

