package com.delycomps.rintisa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.Material

class AdapterMaterial(
    private var listMaterial: MutableList<Material>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterMaterial.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return listMaterial.size
    }

    interface ListAdapterListener { // create an interface
        fun onClickAtDetailMaterial(pointSale: Material, position: Int, type: String)  // create callback function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_material, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun updateItemMaterial(material: Material, index: Int) {
        listMaterial[index].material = material.material
        listMaterial[index].brand = material.brand
        listMaterial[index].quantity = material.quantity
        notifyItemChanged(index)
    }

    fun removeItemMaterial(index: Int) {
        listMaterial.removeAt(index)
        notifyItemRemoved(index)
    }

    fun addMaterial(material: Material) {
        listMaterial.add(material)
        notifyItemInserted(listMaterial.count() - 1)
    }
    fun getList() : List<Material> {
        return listMaterial
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val pointSale: Material = listMaterial[position]

        holder.itemMaterialName.text = pointSale.material
        holder.itemMaterialBrand.text = pointSale.brand
        holder.itemMaterialQuantity.text = pointSale.quantity.toString()
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var itemMaterialName: TextView = itemView.findViewById(R.id.item_material)
        internal var itemMaterialBrand: TextView = itemView.findViewById(R.id.item_brand)
        internal var itemMaterialQuantity: TextView = itemView.findViewById(R.id.item_quantity)
        private var buttonRemove: ImageButton = itemView.findViewById(R.id.button_remove)

        init {
            buttonRemove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val pointSale = listMaterial[position]
                    refListener.onClickAtDetailMaterial(pointSale, position, "DELETE")
                }
            }
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val pointSale = listMaterial[position]
                    refListener.onClickAtDetailMaterial(pointSale, position, "UPDATE")
                }
            }
        }
    }
}

