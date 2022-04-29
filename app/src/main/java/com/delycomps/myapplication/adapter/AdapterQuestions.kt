package com.delycomps.myapplication.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.R
import com.delycomps.myapplication.model.Availability
import com.delycomps.myapplication.model.Material
import com.delycomps.myapplication.model.Question
import com.google.android.material.switchmaterial.SwitchMaterial

class AdapterQuestions(
    private var questionList: MutableList<Question>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterQuestions.OrderViewHolder>() {
    private lateinit var mContext: Context


    override fun getItemCount(): Int {
        return questionList.size
    }

    interface ListAdapterListener { // create an interface
        fun availability(availability: Question, position: Int)  // create callback function
    }

    fun updateQuestion(questionList1: MutableList<Question>) {
        questionList = questionList1.map { r -> Question(r.score, r.text, false) }.toMutableList()
        notifyDataSetChanged()
    }

    fun getList() : List<Question> {
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
        val availability: Question = questionList[position]

        holder.availabilityName.text = availability.text
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

                    val availability = questionList[position]
                    availability.flag = isChecked
                    refListener.availability(availability, position)
                }
            }
        }
    }
}

