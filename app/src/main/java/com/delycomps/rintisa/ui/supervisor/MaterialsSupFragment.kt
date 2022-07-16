package com.delycomps.rintisa.ui.supervisor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.SupervisorViewModel
import com.delycomps.rintisa.adapter.AdapterMaterial
import com.delycomps.rintisa.model.Material

class MaterialsSupFragment : Fragment() {
    private lateinit var viewModel: SupervisorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_materials_sup, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(SupervisorViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv: RecyclerView = view.findViewById(R.id.main_rv_material)
        rv.layoutManager = LinearLayoutManager(view.context)

        viewModel.listMaterial.observe(requireActivity()) {
            rv.adapter = AdapterMaterial((it ?: emptyList()).toMutableList(),  object : AdapterMaterial.ListAdapterListener {
                override fun onClickAtDetailMaterial(material: Material, position: Int, type: String) {

                }
            })
        }
    }
}