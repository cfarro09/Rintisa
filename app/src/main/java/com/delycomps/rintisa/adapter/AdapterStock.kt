package com.delycomps.rintisa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.Stock

class AdapterStock(
    private var listStock: MutableList<Stock>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterStock.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listStock.size
    }

    interface ListAdapterListener { // create an interface
        fun onUpdateStock(stock: Stock, position: Int, type: String)  // create callback function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateItemStock(product: Stock, index: Int) {
        listStock[index].brand = product.brand
        listStock[index].type = product.type
        listStock[index].product = product.product
        notifyItemChanged(index)
    }

    fun removeItemStock(index: Int) {
        listStock.removeAt(index)
        notifyItemRemoved(index)
    }

    fun addStock(p: Stock): Boolean {
        if (listStock.find { it.product == p.product && it.type == p.type && it.brand == p.brand } == null) {
            listStock.add(p)
            notifyItemInserted(listStock.count() - 1)
            return true
        }
        return false
    }
    fun getList() : List<Stock> {
        return listStock
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val product: Stock = listStock[position]

        holder.itemStockProduct.text = product.product
        holder.itemStockBrand.text = product.brand
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var itemStockProduct: TextView = itemView.findViewById(R.id.stock_product)
        internal var itemStockBrand: TextView = itemView.findViewById(R.id.stock_brand)
        var buttonRemove: ImageButton = itemView.findViewById(R.id.stock_remove)

        init {
            buttonRemove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = listStock[position]
                    refListener.onUpdateStock(product, position, "DELETE")
                }
            }
        }
    }
}

