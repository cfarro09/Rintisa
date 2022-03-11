package com.delycomps.myapplication.ui.merchant

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.myapplication.MainActivity
import com.delycomps.myapplication.MerchantActivity
import com.delycomps.myapplication.R
import com.delycomps.myapplication.adapter.AdapterMaterial
import com.delycomps.myapplication.model.Material
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MaterialsFragment : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var viewModel: MerchantViewModel
    private var listMaterial: MutableList<Material> = ArrayList()
    private lateinit var listMaterialNames : List<String>
    private lateinit var listBrand : List<String>
    private var indexSelected = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_materials, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MerchantViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.main_rv_material)
        rv.layoutManager = LinearLayoutManager(view.context)

        viewModel.dataBrands.observe(requireActivity()) {
            listBrand = listOf("Seleccione") + it
        }
        viewModel.dataMaterials.observe(requireActivity()) { it ->
            listMaterialNames = listOf("Seleccione") + it.map { it.material!! }.toList()
            startALL(view)
        }
    }

    private fun manageDialogMaterial (view: View, dialog: AlertDialog) {
        val spinnerMaterial = view.findViewById<Spinner>(R.id.spinner_material)
        val spinnerBrand = view.findViewById<Spinner>(R.id.spinner_brand)
        val editTextQuantity = view.findViewById<EditText>(R.id.dialog_quantity)

        spinnerMaterial.adapter = object : ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, listMaterialNames) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        spinnerBrand.adapter = object : ArrayAdapter<String?>(view.context, android.R.layout.simple_list_item_1, listBrand) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }

        val buttonSave = view.findViewById<Button>(R.id.dialog_save_material)
        val buttonCancel = view.findViewById<Button>(R.id.dialog_cancel_material)

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonSave.setOnClickListener {
            val material = spinnerMaterial.selectedItem.toString()
            val brand = spinnerBrand.selectedItem.toString()
            val quantity = editTextQuantity.text.toString()

            if (material != "" && material != "Seleccione" && brand != "" && brand != "Seleccione" && quantity != "") {
                if (quantity.toInt() <= 0) {
                    Toast.makeText(view.context, "La cantidad debe ser mayor a 0", Toast.LENGTH_LONG).show()
                } else {
                    val material = Material(material, brand, quantity.toInt())
                    if (indexSelected == -1) {
                        (rv.adapter as AdapterMaterial).addMaterial(material)
                        viewModel.addMaterial(material)
                    }
                    else {
                        (rv.adapter as AdapterMaterial).updateItemMaterial(material, indexSelected)
                        viewModel.updateMaterial(material, indexSelected)
                    }
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(view.context, "Debe llenar todos los campos", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun startALL (view : View) {
        val buttonRegister = view.findViewById<FloatingActionButton>(R.id.material_register)

        val builderDialogMaterial: AlertDialog.Builder = AlertDialog.Builder(view.context)
        val inflater = this.layoutInflater
        val dialogMaterialUI = inflater.inflate(R.layout.layout_material_register, null)
        builderDialogMaterial.setView(dialogMaterialUI)
        val dialogMaterial = builderDialogMaterial.create()
        manageDialogMaterial(dialogMaterialUI, dialogMaterial)

        rv.adapter = AdapterMaterial(listMaterial,  object : AdapterMaterial.ListAdapterListener {
            override fun onClickAtDetailMaterial(material: Material, position: Int, type: String) {
                indexSelected = position
                if (type == "UPDATE") {
                    dialogMaterialUI.findViewById<Spinner>(R.id.spinner_material).setSelection(listMaterialNames.indexOf(material.material))
                    dialogMaterialUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(listBrand.indexOf(material.brand))
                    dialogMaterialUI.findViewById<EditText>(R.id.dialog_quantity).text = Editable.Factory.getInstance().newEditable("" + material.quantity)
                    dialogMaterial.show()
                } else {
                    (rv.adapter as AdapterMaterial).removeItemMaterial(position)
                    viewModel.removeMaterial(indexSelected)
                }
            }
        })

        buttonRegister.setOnClickListener {
            indexSelected = -1
            dialogMaterialUI.findViewById<Spinner>(R.id.spinner_material).setSelection(0)
            dialogMaterialUI.findViewById<Spinner>(R.id.spinner_brand).setSelection(0)
            dialogMaterialUI.findViewById<EditText>(R.id.dialog_quantity).text = Editable.Factory.getInstance().newEditable("")
            dialogMaterial.show()
        }
    }
}