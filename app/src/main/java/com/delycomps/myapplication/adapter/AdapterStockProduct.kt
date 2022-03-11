package com.delycomps.myapplication.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.R
import com.delycomps.myapplication.model.Stock
import com.google.android.material.switchmaterial.SwitchMaterial

class AdapterStockProduct(
    private var listProduct: MutableList<Stock>) : RecyclerView.Adapter<AdapterStockProduct.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listProduct.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_stock_product, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun getList() : List<Stock> {
        return listProduct
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val product: Stock = listProduct[position]

        holder.itemProduct.text = product.product
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var itemProduct: TextView = itemView.findViewById(R.id.item_product)
        private var itemCheckbox: CheckBox = itemView.findViewById(R.id.item_checkbox)

        init {
            itemCheckbox.setOnCheckedChangeListener { _, checked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listProduct[position].flag = checked
                }
            }
        }
    }
}

