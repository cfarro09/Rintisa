package com.delycomps.rintisa.ui.supervisor

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.delycomps.rintisa.*
import com.delycomps.rintisa.model.PointSale

class InformationMerchant : Fragment() {
    private lateinit var viewModel: SupervisorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_merchant_information, container, false)
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(requireActivity()).get(SupervisorViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(view.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        val dialogLoading: AlertDialog = builderLoading.create()

        val pointSale: PointSale? = activity?.intent?.getParcelableExtra(Constants.POINT_SALE_ITEM)
        val service: String? = activity?.intent?.getStringExtra(Constants.POINT_SALE_SERVICE)

        if (pointSale != null) {
            view.findViewById<CardView>(R.id.container_comment).visibility = if (service == "MERCADERISMO") View.VISIBLE else View.GONE
            view.findViewById<CardView>(R.id.container_images).visibility = if (service == "MERCADERISMO") View.VISIBLE else View.GONE
            view.findViewById<CardView>(R.id.container_know).visibility = if (service != "MERCADERISMO") View.VISIBLE else View.GONE
            view.findViewById<LinearLayout>(R.id.container_spinner_promoter).visibility = if (service != "MERCADERISMO") View.VISIBLE else View.GONE

            view.findViewById<TextView>(R.id.pdv_client).text = pointSale.client
            view.findViewById<TextView>(R.id.pdv_market).text = pointSale.market
            view.findViewById<TextView>(R.id.pdv_stall_number).text = "N° PUESTO: " + pointSale.stallNumber
            view.findViewById<TextView>(R.id.pdv_date).text = "${pointSale.visitFrequency} (${pointSale.visitDay})"
            view.findViewById<TextView>(R.id.pdv_last_visit).text = pointSale.lastVisit
            view.findViewById<TextView>(R.id.pdv_motive).text = pointSale.management + " - " + pointSale.motive

            val spinnerSpeachScn = view.findViewById<Spinner>(R.id.spinner_speach_scn)
            val spinnerSpeachRcn = view.findViewById<Spinner>(R.id.spinner_speach_rcn)
            val spinnerSpeachRct = view.findViewById<Spinner>(R.id.spinner_speach_rct)
            val spinnerSpeachSct = view.findViewById<Spinner>(R.id.spinner_speach_sct)

            val spinnerUser = view.findViewById<Spinner>(R.id.spinner_promoter)
            spinnerUser.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, viewModel.dataUser.value?.map { it.description } ?: emptyList())


            spinnerUser.setSelection(viewModel.dataUser.value?.indexOfFirst { it.userid == pointSale.userid } ?: 0)

            spinnerUser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val userDesc = spinnerUser.selectedItem.toString()
                    val userid = userDesc.split(".")[0].toDouble().toInt()
                    viewModel.setUserSelected(userid)
                }
            }

            spinnerSpeachScn.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("MUY BIEN", "REFORZAR"))
            spinnerSpeachRcn.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("MUY BIEN", "REFORZAR"))
            spinnerSpeachRct.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("MUY BIEN", "REFORZAR"))
            spinnerSpeachSct.adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, listOf("MUY BIEN", "REFORZAR"))


            spinnerSpeachSct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view1: View?, position: Int, id: Long) {
                    val value = spinnerSpeachSct.selectedItem.toString()
                    viewModel.setSpeechSCT(value)
                }
            }
            spinnerSpeachRct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view1: View?, position: Int, id: Long) {
                    val value = spinnerSpeachRct.selectedItem.toString()
                    viewModel.setSpeechRCT(value)
                }
            }
            spinnerSpeachScn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view1: View?, position: Int, id: Long) {
                    val value = spinnerSpeachScn.selectedItem.toString()
                    viewModel.setSpeechSCN(value)
                }
            }
            spinnerSpeachRcn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) { }
                override fun onItemSelected(parent: AdapterView<*>?, view1: View?, position: Int, id: Long) {
                    val value = spinnerSpeachRcn.selectedItem.toString()
                    viewModel.setSpeechRCN(value)
                }
            }
//            val button = view.findViewById<ImageButton>(R.id.button_save)
//            val buttonKnow = view.findViewById<ImageButton>(R.id.button_know_save)
            val editComment = view.findViewById<EditText>(R.id.text_comment)

            editComment.addTextChangedListener {
                viewModel.setComment(editComment.text.toString())
            }

//            buttonKnow.setOnClickListener {
//                val textSpeachScn = spinnerSpeachScn.selectedItem.toString()
//                val textSpeachRcn = spinnerSpeachRcn.selectedItem.toString()
//                val textSpeachRct = spinnerSpeachRct.selectedItem.toString()
//                val textSpeachSct = spinnerSpeachSct.selectedItem.toString()
//
//                val ob = JSONObject()
//                ob.put("customerid", pointSale.customerId)
//                ob.put("speach_scn", textSpeachScn)
//                ob.put("speach_rcn", textSpeachRcn)
//                ob.put("speach_rct", textSpeachRct)
//                ob.put("speach_sct", textSpeachSct)
//                ob.put("aux_userid", viewModel.userSelected.value)
//
//                viewModel.executeSupervisor(ob, "QUERY_UPDATE_SPEACH1", SharedPrefsCache(view.context).getToken())
//            }

            val color = if (pointSale.trafficLights == "AMARILLO") "#FFF8B7" else if (pointSale.trafficLights == "VERDE") "#B6FFA9" else  "#FF9696"
            view.findViewById<View>(R.id.pdv_traffic_light).backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(color))

            val imageAfter = view.findViewById<ImageView>(R.id.view_image_after)
            val imageBefore = view.findViewById<ImageView>(R.id.view_image_before)

            if ((pointSale.imageBefore ?: "") == "") {
                view.findViewById<TextView>(R.id.text_before).visibility = View.VISIBLE
                imageBefore.visibility = View.GONE
            } else {
                view.findViewById<TextView>(R.id.text_before).visibility = View.GONE
                imageBefore.visibility = View.VISIBLE
                Glide.with(this)
                    .load(pointSale.imageBefore)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.loadingtmp)
                    .into(view.findViewById(R.id.view_image_before))
                imageBefore.setOnClickListener {
                    val intent = Intent(
                        view.context,
                        ImageActivity::class.java
                    )
                    intent.putExtra(Constants.URL_IMAGE, pointSale.imageBefore)
                    startActivity(intent)
                }
            }


            if ((pointSale.imageAfter ?: "") == "") {
                view.findViewById<TextView>(R.id.text_after).visibility = View.VISIBLE
                imageAfter.visibility = View.GONE
            } else {
                view.findViewById<TextView>(R.id.text_after).visibility = View.GONE
                imageAfter.visibility = View.VISIBLE
                Glide.with(this)
                    .load(pointSale.imageAfter)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.loadingtmp)
                    .into(view.findViewById(R.id.view_image_after))

                imageAfter.setOnClickListener {
                    val intent = Intent(
                        view.context,
                        ImageActivity::class.java
                    )
                    intent.putExtra(Constants.URL_IMAGE, pointSale.imageAfter)
                    startActivity(intent)
                }
            }
        }
    }
}