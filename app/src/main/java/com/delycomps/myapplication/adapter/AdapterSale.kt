package com.delycomps.myapplication.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.delycomps.myapplication.R
import com.delycomps.myapplication.model.SurveyProduct

class AdapterSale(
    private var listSurveyProduct: MutableList<SurveyProduct>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterSale.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listSurveyProduct.size
    }

    interface ListAdapterListener { // create an interface
        fun onClickAtDetailProduct(surveyProduct: SurveyProduct, position: Int, type: String)  // create callback function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_sale, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateItemProduct(surveyProduct: SurveyProduct, index: Int) {
        listSurveyProduct[index].measureUnit = surveyProduct.measureUnit
        listSurveyProduct[index].brand = surveyProduct.brand
        listSurveyProduct[index].merchant = surveyProduct.merchant
        listSurveyProduct[index].imageEvidence = surveyProduct.imageEvidence
        listSurveyProduct[index].description = surveyProduct.description
        listSurveyProduct[index].price = surveyProduct.price

        notifyItemChanged(index)
    }

    fun removeItemProduct(index: Int) {
        listSurveyProduct.removeAt(index)
        notifyItemRemoved(index)
    }

    fun addProduct(surveyProduct: SurveyProduct) {
        listSurveyProduct.add(surveyProduct)
        notifyItemInserted(listSurveyProduct.count() - 1)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val surveyProduct: SurveyProduct = listSurveyProduct[position]

        holder.itemProductMeasureUnit.text = surveyProduct.measureUnit
        holder.itemProductQuantity.text = surveyProduct.quantity.toString()
        holder.itemProductProduct.text = surveyProduct.description + " (" + surveyProduct.brand + ")"

        if (surveyProduct.merchant != "") {
            holder.itemProductMerchant.text = surveyProduct.merchant
            holder.itemContainerMerchant.visibility = View.VISIBLE
        } else {
            holder.itemContainerMerchant.visibility = View.GONE
        }

        holder.itemProductLoading.visibility = View.VISIBLE
        Glide.with(holder.itemProductMeasureUnit.context)
            .load(surveyProduct.imageEvidence)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.itemProductLoading.visibility = View.GONE
                    return false
                }
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.itemProductLoading.visibility = View.GONE
                    return false
                }
            })
            .into(holder.itemProductImage)
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var itemProductMeasureUnit: TextView = itemView.findViewById(R.id.item_measure_unit)
        internal var itemProductProduct: TextView = itemView.findViewById(R.id.item_product)
        internal var itemProductQuantity: TextView = itemView.findViewById(R.id.item_quantity)
        internal var itemContainerMerchant: LinearLayout = itemView.findViewById(R.id.container_merchant)
        internal var itemProductMerchant: TextView = itemView.findViewById(R.id.item_merchant)
        internal var itemProductImage: ImageView = itemView.findViewById(R.id.view_image_evidence)
        internal var itemProductLoading: ProgressBar = itemView.findViewById(R.id.loading_evidence)
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

