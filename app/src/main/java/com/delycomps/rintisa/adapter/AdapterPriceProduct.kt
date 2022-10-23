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
import com.delycomps.rintisa.model.PriceProduct
import java.lang.Exception

class AdapterPriceProduct(
    private var listProduct: MutableList<PriceProduct>) : RecyclerView.Adapter<AdapterPriceProduct.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listProduct.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_price_product, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun getList() : List<PriceProduct> {
        return listProduct
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val product: PriceProduct = listProduct[position]

        holder.itemProduct.text = product.description
        if (product.category == "SECOS") {
            holder.itemS.visibility = View.VISIBLE
        } else {
            holder.itemS.visibility = View.GONE
        }
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var itemProduct: TextView = itemView.findViewById(R.id.item_name)
        private var itemK: EditText = itemView.findViewById(R.id.item_k)
        internal var itemS: EditText = itemView.findViewById(R.id.item_s)
//        private var itemCheckbox: CheckBox = itemView.findViewById(R.id.item_checkbox)

        init {
            itemK.addTextChangedListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        listProduct[position].price_k = (if (itemK.text.toString() == "") "0" else itemK.text.toString()).toDouble()
                    } catch (e: Exception) {
                        itemK.text = Editable.Factory.getInstance().newEditable("")
                    }
                }
            }
            itemS.addTextChangedListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {

                    try {
                        listProduct[position].price_s = (if (itemS.text.toString() == "") "0" else itemS.text.toString()).toDouble()
                    } catch (e: Exception) {

                    }
                }
            }
        }
    }
}

