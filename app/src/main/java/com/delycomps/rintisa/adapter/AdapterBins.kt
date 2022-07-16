package com.delycomps.rintisa.adapter

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.CheckSupPromoter
import java.lang.Exception

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
                    try {
                        if (textValue.text.toString() == "") {
                            questionList[position].value = "0"
                        } else {
                            val ax = textValue.text.toString().toDouble()

                            if (ax > 5) {
                                textValue.text = Editable.Factory.getInstance().newEditable("")
                                questionList[position].value = ""
                            } else {
                                questionList[position].value = textValue.text.toString()
                            }
                        }
                    } catch (e: Exception) {
                        textValue.text = Editable.Factory.getInstance().newEditable("")
                        questionList[position].value = ""

                    }
                }
            }
        }
    }
}

