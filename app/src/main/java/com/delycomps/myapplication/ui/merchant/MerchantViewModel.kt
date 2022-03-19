package com.delycomps.myapplication.ui.merchant

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delycomps.myapplication.api.Repository
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.model.*
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File

class MerchantViewModel : ViewModel() {

    private val _dataBrands: MutableLiveData<List<String>> = MutableLiveData()
    val dataBrands: LiveData<List<String>> = _dataBrands

    private val _dataMaterials: MutableLiveData<List<Material>> = MutableLiveData()
    val dataMaterials: LiveData<List<Material>> = _dataMaterials

    private val _dataProducts: MutableLiveData<List<SurveyProduct>> = MutableLiveData()
    val dataProducts: LiveData<List<SurveyProduct>> = _dataProducts

    private val _listMaterialSelected: MutableLiveData<MutableList<Material>> = MutableLiveData()
    val listMaterialSelected: LiveData<MutableList<Material>> = _listMaterialSelected

    private val _listProductSelected: MutableLiveData<MutableList<SurveyProduct>> = MutableLiveData()
    val listProductSelected: LiveData<MutableList<SurveyProduct>> = _listProductSelected

    private val _urlBeforeImage: MutableLiveData<String> = MutableLiveData()
    val urlBeforeImage: LiveData<String> = _urlBeforeImage

    private val _urlAfterImage: MutableLiveData<String> = MutableLiveData()
    val urlAfterImage: LiveData<String> = _urlAfterImage

    private val _closingMerchant: MutableLiveData<Boolean> = MutableLiveData()
    val closingMerchant: LiveData<Boolean> = _closingMerchant

    private val _urlImageWithBD: MutableLiveData<ResGlobal> = MutableLiveData()
    val urlImageWithBD: LiveData<ResGlobal> = _urlImageWithBD

    fun addMaterial (material: Material, context: Context, visitId: Int) {
        BDLocal(context).addMaterialStock(material, visitId)
        _listMaterialSelected.value = ((_listMaterialSelected.value ?: emptyList()) + listOf(material)).toMutableList()
    }

    fun updateMaterial (material: Material, i: Int, context: Context) {
        material.uuid = _listMaterialSelected.value!![i].uuid
        BDLocal(context).updateMaterialsFromVisit(material)
        _listMaterialSelected.value = _listMaterialSelected.value!!.mapIndexed { index, item -> if (index == i) material else item }.toMutableList()
    }

    fun removeMaterial (i: Int, context: Context, visitId: Int, uuid: String) {
        BDLocal(context).deleteMaterialsFromVisit(visitId, uuid)
        _listMaterialSelected.value = _listMaterialSelected.value!!.filterIndexed { index, _ -> index != i } .toMutableList()
    }

    fun initialMaterialSelected (list: MutableList<Material>) {
        _listMaterialSelected.value = list
    }

    fun addProduct (material: SurveyProduct) {
        _listProductSelected.value = ((_listProductSelected.value ?: emptyList()) + listOf(material)).toMutableList()
    }

    fun updateProduct (product: SurveyProduct, i: Int) {
        _listProductSelected.value = _listProductSelected.value!!.mapIndexed { index, item -> if (index == i) product else item }.toMutableList()
    }

    fun removeProduct (i: Int) {
        _listProductSelected.value = _listProductSelected.value!!.filterIndexed { index, _ -> index != i } .toMutableList()
    }

    fun uploadWithBD(file: File, visitId: Int, type: String, token: String) {
        _urlImageWithBD.value = ResGlobal(true, "", false)
        val method = if (type == "AFTER") "QUERY_IMAGE_AFTER" else "QUERY_IMAGE_BEFORE"
        val jsonRb = Gson().toJson(RequestBodyX(method, method, mapOf<String, Any>(
            "visitid" to visitId
        )))

        Repository().uploadImage(file, token, jsonRb) { isSuccess, result, _ ->
            if (isSuccess) {
                _urlImageWithBD.value = ResGlobal(false, result.toString(), true)
            } else {
                _urlImageWithBD.value = ResGlobal(false, "", false)
            }
        }
    }

    fun uploadBeforeImage(file: File, token: String) {
        Repository().uploadImage(file, token) { isSuccess, result, _ ->
            if (isSuccess) {
                _urlBeforeImage.value = result!!
            } else {
                _urlBeforeImage.value = ""
                Log.d("log_carlos", "URL VACIA")
            }
        }
    }

    fun closeMerchant(visitId: Int, image_before: String, image_after: String, material_list: String, price_survey_list: String, haveSurvey: Boolean, token: String) {
        Repository().insCloseManageMerchant(visitId, image_before, image_after, material_list, price_survey_list, haveSurvey, token) { isSuccess, _ ->
            _closingMerchant.value = isSuccess
        }
    }

    fun uploadAfterImage(file: File, token: String) {
        Repository().uploadImage(file, token) { isSuccess, result, _ ->
            if (isSuccess) {
                _urlAfterImage.value = result!!
            } else {
                Log.d("log_carlos", "URL VACIA")
                _urlAfterImage.value = ""
            }
        }
    }

    fun getMainMulti(token: String) {
        Repository().getMultiMerchant(token) { isSuccess, result, _ ->
            if (isSuccess) {
                _dataBrands.value = result?.brands ?: emptyList()
                _dataMaterials.value = result?.materials ?: emptyList()
                _dataProducts.value = result?.products ?: emptyList()
            }
        }
    }

    fun initMainMulti(data: DataMerchant) {
//        Repository().getMultiMerchant(token) { isSuccess, result, _ ->
//            if (isSuccess) {
            _dataBrands.value = data.brands
            _dataMaterials.value = data.materials
            _dataProducts.value = data.products
//            }
//        }
    }
}