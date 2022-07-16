package com.delycomps.rintisa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.SurveyProduct

class AdapterProductSurvey(
    private var listSurveyProduct: MutableList<SurveyProduct>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterProductSurvey.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listSurveyProduct.size
    }

    interface ListAdapterListener { // create an interface
        fun onClickAtDetailProduct(surveyProduct: SurveyProduct, position: Int, type: String)  // create callback function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateItemProduct(surveyProduct: SurveyProduct, index: Int) {
        listSurveyProduct[index].measureUnit = surveyProduct.measureUnit
        listSurveyProduct[index].description = surveyProduct.description
        listSurveyProduct[index].price = surveyProduct.price

        notifyItemChanged(index)
    }

    fun removeItemProduct(index: Int) {
        listSurveyProduct.removeAt(index)
        notifyItemRemoved(index)
    }

    fun addProduct(surveyProduct: SurveyProduct): Boolean {
        if (listSurveyProduct.find { it.productId == surveyProduct.productId && it.measureUnit == surveyProduct.measureUnit } == null) {
            listSurveyProduct.add(surveyProduct)
            notifyItemInserted(listSurveyProduct.count() - 1)
            return true
        }
        return false
    }
    fun getList() : List<SurveyProduct> {
        return listSurveyProduct
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val surveyProduct: SurveyProduct = listSurveyProduct[position]

        holder.itemProductMeasureUnit.text = surveyProduct.measureUnit
        holder.itemProductPrice.text = surveyProduct.price.toString()
        holder.itemProductProduct.text = surveyProduct.description + " (" + surveyProduct.brand + ")"
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var itemProductMeasureUnit: TextView = itemView.findViewById(R.id.item_measure_unit)
        internal var itemProductProduct: TextView = itemView.findViewById(R.id.item_product)
        internal var itemProductPrice: TextView = itemView.findViewById(R.id.item_price)
        private var buttonRemove: ImageButton = itemView.findViewById(R.id.button_remove)

        init {
            buttonRemove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = listSurveyProduct[position]
                    refListener.onClickAtDetailProduct(product, position, "DELETE")
                }
            }
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = listSurveyProduct[position]
                    refListener.onClickAtDetailProduct(product, position, "UPDATE")
                }
            }
        }
    }
}

