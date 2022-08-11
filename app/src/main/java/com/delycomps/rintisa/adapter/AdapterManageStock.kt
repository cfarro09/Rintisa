package com.delycomps.rintisa.adapter

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.Merchandise
import java.lang.Exception

class AdapterManageStock(
    private var listMerchandise: List<Merchandise>) : RecyclerView.Adapter<AdapterManageStock.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listMerchandise.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_close_stock, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun getList() : List<Merchandise> {
        return listMerchandise
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val merchandise: Merchandise = listMerchandise[position]

        holder.description.text = merchandise.description
        if ((merchandise.quantity ?: 0) != 0) {
            holder.quantity.text = Editable.Factory.getInstance().newEditable((merchandise.quantity).toString() )
        } else {
            holder.quantity.text = Editable.Factory.getInstance().newEditable("")
        }
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var description: TextView = itemView.findViewById(R.id.stock_product_description)
        var quantity: EditText = itemView.findViewById(R.id.stock_product_quantity)

        init {
            quantity.addTextChangedListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        listMerchandise[position].quantity = (if (quantity.text.toString() == "") "0" else quantity.text.toString()).toInt()
                    } catch (e: Exception) {
                        quantity.text = Editable.Factory.getInstance().newEditable("")
                    }
                }
            }
        }
    }
}

