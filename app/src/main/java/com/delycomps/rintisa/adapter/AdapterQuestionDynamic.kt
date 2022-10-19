package com.delycomps.rintisa.adapter

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.model.CheckSupPromoter
import com.google.android.material.switchmaterial.SwitchMaterial
import java.lang.Exception

class AdapterQuestionDynamic(
    private var questionList: MutableList<CheckSupPromoter>
) : RecyclerView.Adapter<AdapterQuestionDynamic.OrderViewHolder>() {
    private lateinit var mContext: Context

    override fun getItemCount(): Int {
        return questionList.size
    }

    fun getList() : List<CheckSupPromoter> {
        return questionList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_questiondynamic, parent, false)
        mContext = v.context
        return OrderViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val question: CheckSupPromoter = questionList[position]
        holder.questionDescription.text = question.decription
        if (question.type == "") {
            holder.questionType.visibility = View.GONE
            holder.questionLine.visibility = View.GONE
        } else {
            holder.questionType.text = question.type
            holder.questionLine.visibility = View.VISIBLE
        }
        val type = question.key.split("||")[1]
        val key = question.key.split("||")[0]
        when (type) {
            "switch" -> {
                holder.questionSwitch.isChecked = question.flag
                holder.questionSwitch.visibility = View.VISIBLE
            }
            "number" -> {
                holder.questionNumber.text = Editable.Factory.getInstance().newEditable(question.value)
                holder.questionNumber.visibility = View.VISIBLE
            }
            "text" -> {
                holder.questionText.text = Editable.Factory.getInstance().newEditable(question.value)
                holder.questionText.visibility = View.VISIBLE
            }
            "select" -> {
                holder.questionSelect.visibility = View.VISIBLE
                var list: List<String> = emptyList()
                if (key == "lineanimiento") {
                    list = listOf("Bueno", "Regular", "Malo")
                }
                holder.questionSelect.adapter = ArrayAdapter( mContext, android.R.layout.simple_dropdown_item_1line, list)
            }
        }
    }

    inner class OrderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var questionDescription: TextView = itemView.findViewById(R.id.question_description)
        var questionNumber: EditText = itemView.findViewById(R.id.question_number)
        var questionSwitch: SwitchMaterial = itemView.findViewById(R.id.question_switch)
        var questionText: EditText = itemView.findViewById(R.id.question_text)
        var questionType: TextView = itemView.findViewById(R.id.question_type)
        var questionSelect: Spinner = itemView.findViewById(R.id.question_select)
        var questionLine: View = itemView.findViewById(R.id.question_line)

        init {
            questionSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val question = questionList[position]
                        question.value = questionSelect.selectedItem.toString()
                    }
                }
            }
            questionSwitch.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val question = questionList[position]
                    question.flag = isChecked
                }
            }
            questionNumber.addTextChangedListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        if (questionNumber.text.toString() == "") {
                            questionList[position].value = "0"
                        } else {
                            val ax = questionNumber.text.toString().toDouble()

                            if (ax > 5) {
                                questionNumber.text = Editable.Factory.getInstance().newEditable("")
                                questionList[position].value = ""
                            } else {
                                questionList[position].value = questionNumber.text.toString()
                            }
                        }
                    } catch (e: Exception) {
                        questionNumber.text = Editable.Factory.getInstance().newEditable("")
                        questionList[position].value = ""

                    }
                }
            }
            questionText.addTextChangedListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    questionList[position].value = questionText.text.toString()
                }
            }
        }
    }
}

