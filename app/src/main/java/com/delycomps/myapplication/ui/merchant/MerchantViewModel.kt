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

    private val _management: MutableLiveData<Management> = MutableLiveData()
    val management: LiveData<Management> = _management

    private val _dataMaterials: MutableLiveData<List<Material>> = MutableLiveData()
    val dataMaterials: LiveData<List<Material>> = _dataMaterials

    private val _dataProducts: MutableLiveData<List<SurveyProduct>> = MutableLiveData()
    val dataProducts: LiveData<List<SurveyProduct>> = _dataProducts

    private val _listMaterialSelected: MutableLiveData<MutableList<Material>> = MutableLiveData()
    val listMaterialSelected: LiveData<MutableList<Material>> = _listMaterialSelected

    private val _listProductSelected: MutableLiveData<MutableList<SurveyProduct>> = MutableLiveData()
    val listProductSelected: LiveData<MutableList<SurveyProduct>> = _listProductSelected

    private val _productsAvailability: MutableLiveData<MutableList<Availability>> = MutableLiveData()
    val productsAvailability: LiveData<MutableList<Availability>> = _productsAvailability

    private val _urlBeforeImage: MutableLiveData<String> = MutableLiveData()
    val urlBeforeImage: LiveData<String> = _urlBeforeImage

    private val _urlAfterImage: MutableLiveData<String> = MutableLiveData()
    val urlAfterImage: LiveData<String> = _urlAfterImage

    private val _urlImage: MutableLiveData<String> = MutableLiveData()
    val urlImage: LiveData<String> = _urlImage

    private val _closingMerchant: MutableLiveData<Boolean> = MutableLiveData()
    val closingMerchant: LiveData<Boolean> = _closingMerchant

    private val _urlImageWithBD: MutableLiveData<ResGlobal> = MutableLiveData()
    val urlImageWithBD: LiveData<ResGlobal> = _urlImageWithBD

    fun setManagement (management: Management) {

        _management.value = Management(
            management.status_management ?: _management.value?.status_management ?: "",
            management.motive ?: _management.value?.motive ?: "",
            management.observation ?: _management.value?.observation ?: "",
        )
    }


    fun addMaterial (material: Material, context: Context, visitId: Int) {
        BDLocal(context).addMaterialStock(material, visitId)
        _listMaterialSelected.value = ((_listMaterialSelected.value ?: emptyList()) + listOf(material)).toMutableList()
    }

    fun initialProductAvailability (list: MutableList<Availability>) {
        _productsAvailability.value = list
    }

    fun manageProductAvailability (product: Availability, context: Context, visitId: Int) {
//        BDLocal(context).addMaterialStock(material, visitId)
        if (product.flag == true) {
            _productsAvailability.value = ((_productsAvailability.value ?: emptyList()) + listOf(product)).toMutableList()
            BDLocal(context).addProductsAvailability(product, visitId)
        } else {
            val uuid = _productsAvailability.value?.find { it.productid == product.productid }?.uuid ?: ""
            BDLocal(context).deleteProductAvailability(uuid)
            _productsAvailability.value = _productsAvailability.value!!.filter { it.productid != product.productid }.toMutableList()
        }
    }

    fun updateMaterial (material: Material, i: Int, context: Context) {
        material.uuid = _listMaterialSelected.value!![i].uuid
        BDLocal(context).updateMaterialsFromVisit(material)
        _listMaterialSelected.value = _listMaterialSelected.value!!.mapIndexed { index, item -> if (index == i) material else item }.toMutableList()
    }

    fun removeMaterial (i: Int, context: Context, uuid: String) {
        BDLocal(context).deleteMaterial(uuid)
        _listMaterialSelected.value = _listMaterialSelected.value!!.filterIndexed { index, _ -> index != i } .toMutableList()
    }

    fun initialMaterialSelected (list: MutableList<Material>) {
        _listMaterialSelected.value = list
    }

    fun initialPriceProduct (list: MutableList<SurveyProduct>) {
        _listProductSelected.value = list
    }


    fun addProduct (stocks: List<SurveyProduct>) {
        _listProductSelected.value = ((_listProductSelected.value ?: emptyList()) + stocks.filter { (_listProductSelected.value ?: emptyList()).find { r -> r.productId == it.productId && r.measureUnit == it.measureUnit } == null }).toMutableList()
//        _listProductSelected.value = ((_listProductSelected.value ?: emptyList()) + listOf(material)).toMutableList()
    }

    fun updateProduct (product: SurveyProduct, i: Int, context: Context) {
        product.uuid = _listProductSelected.value!![i].uuid
        _listProductSelected.value = _listProductSelected.value!!.mapIndexed { index, item -> if (index == i) product else item }.toMutableList()
        BDLocal(context).updateMerchantPrice(product)
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

    fun uploadImage(file: File, token: String) {
        Repository().uploadImage(file, token) { isSuccess, result, _ ->
            if (isSuccess) {
                _urlImage.value = result!!
            } else {
                _urlImage.value = ""
            }
        }
    }

    fun uploadImageLocal(path: String, type: String) {
        if (type == "BEFORE") {
            _urlBeforeImage.value = path
        } else {
            _urlAfterImage.value = path
        }
    }

    fun closeMerchant(visitId: Int, image_before: String, image_after: String, material_list: String, price_survey_list: String, haveSurvey: Boolean,
                      listAvailability: String, haveAvailability: Boolean,
                      status_management: String, motive: String, observation: String, token: String, finishDate: String? = null) {
        Repository().insCloseManageMerchant(visitId, image_before, image_after, material_list, price_survey_list, haveSurvey,
            listAvailability,
            haveAvailability,
            status_management, motive, observation, token, finishDate) { isSuccess, _ ->
            _closingMerchant.value = isSuccess
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
            _dataBrands.value = data.brands
            _dataMaterials.value = data.materials
            _dataProducts.value = data.products
    }
}