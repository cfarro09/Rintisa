package com.delycomps.myapplication.ui.promoter

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.delycomps.myapplication.api.Repository
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.model.*
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PromoterViewModel : ViewModel() {

    private val _dataMerchandise: MutableLiveData<List<Merchandise>> = MutableLiveData()
    val dataMerchandise: LiveData<List<Merchandise>> = _dataMerchandise

    private val _dataStocks: MutableLiveData<List<Stock>> = MutableLiveData()
    val dataStocks: LiveData<List<Stock>> = _dataStocks

    private val _dataBrandSale: MutableLiveData<List<BrandSale>> = MutableLiveData()
    val dataBrandSale: LiveData<List<BrandSale>> = _dataBrandSale

    private val _loadingInital: MutableLiveData<Boolean> = MutableLiveData()
    val loadingInital: LiveData<Boolean> = _loadingInital

    private val _listProductSelected: MutableLiveData<MutableList<SurveyProduct>> = MutableLiveData()
    val listProductSelected: LiveData<MutableList<SurveyProduct>> = _listProductSelected

    private val _listStockSelected: MutableLiveData<MutableList<Stock>> = MutableLiveData()
    val listStockSelected: LiveData<MutableList<Stock>> = _listStockSelected

    private val _closingPromoter: MutableLiveData<Boolean> = MutableLiveData()
    val closingPromoter: LiveData<Boolean> = _closingPromoter

    private val _loadingSelfie: MutableLiveData<Boolean> = MutableLiveData()
    val loadingSelfie: LiveData<Boolean> = _loadingSelfie

    private val _urlSelfie: MutableLiveData<String> = MutableLiveData()
    val urlSelfie: LiveData<String> = _urlSelfie

    fun initialDataPromoter(stocks: List<Stock>, products: List<SurveyProduct>) {
        _listStockSelected.value = stocks.toMutableList()
        _listProductSelected.value = products.toMutableList()
    }
    fun setMultiInitial(data: DataPromoter) {
        _dataMerchandise.value = data.merchandises
        _dataBrandSale.value = data.saleBrand
        _dataStocks.value = data.stocks
    }

    fun getMainMultiInitial(token: String) {
//        _loadingInital.value = true
        Repository().getMultiPromoterInitial(token) { isSuccess, result, _ ->
            if (isSuccess) {
                _dataMerchandise.value = result?.merchandises ?: emptyList()
                _dataBrandSale.value = result?.saleBrand ?: emptyList()
                _dataStocks.value = result?.stocks ?: emptyList()
//                _listStockSelected.value = result?.stocksSelected?.toMutableList() ?: ArrayList()
//                _listProductSelected.value = result?.productsSelected?.toMutableList() ?: ArrayList()
//                _loadingInital.value = false
            }
        }
    }

    fun closePromoter(visitId: Int, material_list: String, sale_list: String, showSale: Boolean, merchandises: String, token: String, finish_date: String? = null) {
        Repository().insCloseManagePromoter(visitId, material_list, sale_list, showSale, merchandises, token, finish_date) { isSuccess, _ ->
            _closingPromoter.value = isSuccess
        }
    }

    fun addStocks(stocks: List<Stock>): MutableList<Stock> {
        _listStockSelected.value = ((_listStockSelected.value ?: emptyList()) + stocks.filter { (_listStockSelected.value ?: emptyList()).find { r -> r.product == it.product } == null }).toMutableList()

        return _listStockSelected.value!!
    }

    fun removeStock(i: Int): MutableList<Stock> {
        _listStockSelected.value = _listStockSelected.value!!.filterIndexed { index, _ -> index != i } .toMutableList()
        return _listStockSelected.value!!
    }

    fun addProduct (material: SurveyProduct, visitId: Int, context: Context): MutableList<SurveyProduct> {
        _listProductSelected.value = ((_listProductSelected.value ?: emptyList()) + listOf(material)).toMutableList()
        BDLocal(context).addSalePromoter(material, visitId)
        return _listProductSelected.value!!
    }

    fun updateProduct (product: SurveyProduct, i: Int, context: Context) : MutableList<SurveyProduct> {
        product.uuid = _listProductSelected.value!![i].uuid.toString()
        BDLocal(context).updateSalePromoter(product)
        _listProductSelected.value = _listProductSelected.value!!.mapIndexed { index, item -> if (index == i) product else item }.toMutableList()
        return _listProductSelected.value!!
    }

    fun updateProductImage (uuid: String, urlImage: String, context: Context) {
        val product = _listProductSelected.value!!.find { it.uuid == uuid }
        product?.imageEvidence = urlImage
        BDLocal(context).updateSalePromoter(product!!)
        _listProductSelected.value = _listProductSelected.value!!.map { if (it.uuid == uuid) product else it }.toMutableList()
    }

    fun updateProductImageLocal (uuid: String, urlImage: String, context: Context) {
        BDLocal(context).updateSalePromoterOne(uuid, urlImage)
    }

    fun updateMerchandise (merchandise: Merchandise, i: Int) {
        _dataMerchandise.value = _dataMerchandise.value!!.mapIndexed { index, item -> if (index == i) merchandise else item }.toMutableList()
    }

    fun removeProduct (i: Int, context: Context): MutableList<SurveyProduct> {
        val uuid: String = _listProductSelected.value!![i].uuid.toString()
        BDLocal(context).deleteSalePromoter(uuid)
        _listProductSelected.value = _listProductSelected.value!!.filterIndexed { index, _ -> index != i } .toMutableList()
        return _listProductSelected.value!!
    }

    fun setUrlSelfie(url: String) {
        _urlSelfie.value = url
    }

    fun uploadSelfie(file: File, token: String) {
        _loadingSelfie.value = false
        Repository().uploadImage(file, token) { isSuccess, result, _ ->
            if (isSuccess) {
                _urlSelfie.value = result!!
            } else {
                _urlSelfie.value = ""
            }
            _loadingSelfie.value = true
        }
    }

    fun updateStock(visitId: Int, json: String, token: String) {
        Repository().updatePromoter(visitId, "QUERY_UPDATE_REPLACE_STOCK", json, "replace_stock", token) { isSuccess, _ ->
            if (isSuccess) {

            }
        }
    }
    fun updateSales(visitId: Int, list : List<SurveyProduct>, token: String) {
        val toSend = Gson().toJson(list.map { mapOf<String, Any>(
            "subtotal" to 0,
            "total" to 0,
            "description_sale" to SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(
                Date()
            ),
            "status_sale" to "ACTIVO",
            "type_sale" to "NINGUNO",
            "productid" to 0,
            "quantity" to it.quantity,
            "measure_unit" to (it.measureUnit ?: ""),
            "price" to 0,
            "merchant" to (it.merchant ?: ""),
            "url_evidence" to (it.imageEvidence ?: ""),
            "total_detail" to 0,
            "description_detail" to (it.description ?: ""),
            "status_detail" to "ACTIVO",
            "type_detail" to "NINGUNO",
            "operation" to "INSERT",
        ) }.toList())
        Log.d("davidddd", toSend)
        Repository().updatePromoter(visitId, "UFN_SALEDETAIL_UPDATE", toSend, "sales", token) { isSuccess, _ ->
            if (isSuccess) {

            }
        }
    }
}