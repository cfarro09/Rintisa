package com.delycomps.rintisa.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.Constants
import com.delycomps.rintisa.ImageActivity
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.Merchandise
import com.delycomps.rintisa.model.SurveyProduct

class AdapterSale(
    private var listSurveyProduct: MutableList<SurveyProduct>,
    private var listMerchandise: List<Merchandise>,
    private val refListener: ListAdapterListener,
    private val refListenerMerchant: ListAdapterListenerMerchant
) : RecyclerView.Adapter<AdapterSale.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listSurveyProduct.size
    }

    interface ListAdapterListener { // create an interface
        fun onClickAtDetailProduct(surveyProduct: SurveyProduct, position: Int, type: String)  // create callback function
    }

    interface ListAdapterListenerMerchant { // create an interface
        fun onChangeMerchant(surveyProduct: SurveyProduct, position: Int)  // create callback function
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
        listSurveyProduct[index].category = surveyProduct.category
//        listSurveyProduct[index].merchant = surveyProduct.merchant
        listSurveyProduct[index].imageEvidence = surveyProduct.imageEvidence
        listSurveyProduct[index].imageEvidenceLocal = surveyProduct.imageEvidenceLocal
        listSurveyProduct[index].description = surveyProduct.description
        listSurveyProduct[index].price = surveyProduct.price
        listSurveyProduct[index].quantity = surveyProduct.quantity

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
        holder.itemProductProduct.text = surveyProduct.brand

        val brand = surveyProduct.brand
        val measureUnit = surveyProduct.measureUnit
        val quantity = surveyProduct.quantity

        if (brand == "RICOCAN" || brand == "RICOCAT" || brand == "SUPERCAN" || brand == "SUPERCAT") {
            holder.itemProductMerchant.adapter = ArrayAdapter(holder.itemProductMerchant.context, android.R.layout.simple_spinner_item, listOf("NINGUNO") + listMerchandise.map { it.description })
            holder.itemProductMerchant.setSelection((listOf("NINGUNO") + listMerchandise.map { it.description }).indexOf(surveyProduct.merchant))
            holder.itemContainerMerchant.visibility = View.VISIBLE
        } else {
            holder.itemContainerMerchant.visibility = View.GONE
        }
        if ((surveyProduct.imageEvidenceLocal ?: "") != "") {
            holder.itemProductImage.setImageBitmap(BitmapFactory.decodeFile(surveyProduct.imageEvidenceLocal))
        }
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var itemProductMeasureUnit: TextView = itemView.findViewById(R.id.item_measure_unit)
        internal var itemProductProduct: TextView = itemView.findViewById(R.id.item_product)
        internal var itemProductQuantity: TextView = itemView.findViewById(R.id.item_quantity)
        internal var itemContainerMerchant: RelativeLayout = itemView.findViewById(R.id.container_merchant)
        internal var itemProductMerchant: Spinner = itemView.findViewById(R.id.item_merchant)
        internal var itemProductImage: ImageView = itemView.findViewById(R.id.view_image_evidence)

        private var buttonRemove: ImageButton = itemView.findViewById(R.id.button_remove)
        private var buttonSave: ImageButton = itemView.findViewById(R.id.button_save)
        private var check = 0
        init {
            buttonSave.setOnClickListener {
                itemProductMerchant.isEnabled = false
                itemProductMerchant.isClickable = false
                Toast.makeText(mContext, "¡Se guardó con exito!", Toast.LENGTH_LONG).show()
            }

            itemProductMerchant.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (++check > 1) {
                        val merchant = itemProductMerchant.selectedItem.toString()
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val product = listSurveyProduct[position]
                            product.merchant = merchant
                            refListenerMerchant.onChangeMerchant(product, position)
                        }
                    }
                }
            }

            itemProductImage.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val intent = Intent(
                        mContext,
                        ImageActivity::class.java
                    )
                    intent.putExtra(Constants.URL_IMAGE, listSurveyProduct[position].imageEvidenceLocal)
                    mContext.startActivity(intent)
                }
            }

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

