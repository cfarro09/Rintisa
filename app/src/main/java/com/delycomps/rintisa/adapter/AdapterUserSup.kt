package com.delycomps.rintisa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.Availability
import com.delycomps.rintisa.model.UserSup
import com.google.android.material.switchmaterial.SwitchMaterial

class AdapterUserSup(
    private var userList: List<UserSup>,
    private val refListener: ListAdapterListener
) : RecyclerView.Adapter<AdapterUserSup.OrderViewHolder>() {
    private lateinit var mContext: Context

    interface ListAdapterListener { // create an interface
        fun clickItem(user: UserSup)  // create callback function
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun getList() : List<UserSup> {
        return userList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_usersup, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val user: UserSup = userList[position]

        val efectivity = ((user.finishVisit / (user.finishVisit + user.initiatedVisit + user.withoutVisit)) * 100)

        holder.user.text = user.User
        holder.efectivity.text = "$efectivity%"
        holder.hourEntry.text = "Entrada " + (user.hourEntry ?: "")
        if ((user.hourExit ?: "") != "") {
            holder.hourFinish.text = "Salida " + user.hourExit
        }
        if ((user.hourInitBreak ?: "") != "") {
            holder.hourBreak.text = "Break " + "${user.hourInitBreak ?: ""} - ${user.hourFinishBreak ?: ""}"
        }
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        internal var user: TextView = itemView.findViewById(R.id.user_sup_user)
        internal var efectivity: TextView = itemView.findViewById(R.id.user_sup_efectivity)
        internal var hourEntry: TextView = itemView.findViewById(R.id.user_sup_hour_entry)
        internal var hourFinish: TextView = itemView.findViewById(R.id.user_sup_hour_finish)
        internal var hourBreak: TextView = itemView.findViewById(R.id.user_sup_break)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val pointSale = userList[position]
                refListener.clickItem(pointSale)
            }
        }

    }
}

