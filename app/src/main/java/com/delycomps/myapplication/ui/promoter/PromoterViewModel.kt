package com.delycomps.myapplication.ui.promoter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.delycomps.myapplication.api.Repository
import com.delycomps.myapplication.model.Material
import com.delycomps.myapplication.model.Merchandise
import com.delycomps.myapplication.model.Stock
import com.delycomps.myapplication.model.SurveyProduct

class PromoterViewModel : ViewModel() {

    private val _dataMerchandise: MutableLiveData<List<Merchandise>> = MutableLiveData()
    val dataMerchandise: LiveData<List<Merchandise>> = _dataMerchandise

    private val _dataStocks: MutableLiveData<List<Stock>> = MutableLiveData()
    val dataStocks: LiveData<List<Stock>> = _dataStocks

    private val _dataProducts: MutableLiveData<List<SurveyProduct>> = MutableLiveData()
    val dataProducts: LiveData<List<SurveyProduct>> = _dataProducts

    private val _loadingInital: MutableLiveData<Boolean> = MutableLiveData()
    val loadingInital: LiveData<Boolean> = _loadingInital

    private val _listProductSelected: MutableLiveData<MutableList<SurveyProduct>> = MutableLiveData()
    val listProductSelected: LiveData<MutableList<SurveyProduct>> = _listProductSelected

    private val _listStockSelected: MutableLiveData<MutableList<Stock>> = MutableLiveData()
    val listStockSelected: LiveData<MutableList<Stock>> = _listStockSelected

    private val _closingPromoter: MutableLiveData<Boolean> = MutableLiveData()
    val closingPromoter: LiveData<Boolean> = _closingPromoter

    fun getMainMulti(token: String) {
        _loadingInital.value = true
        Repository().getMultiPromoter(token) { isSuccess, result, _ ->
            if (isSuccess) {
                _dataMerchandise.value = result?.merchandises ?: emptyList()
                _dataProducts.value = result?.products ?: emptyList()
                _dataStocks.value = result?.stocks ?: emptyList()
                _loadingInital.value = false
            }
        }
    }

    fun closePromoter(visitId: Int, material_list: String, sale_list: String, showSale: Boolean, merchandises: String, token: String) {
        Repository().insCloseManagePromoter(visitId, material_list, sale_list, showSale, merchandises, token) { isSuccess, _ ->
            _closingPromoter.value = isSuccess
        }
    }

    fun addStocks(stocks: List<Stock>) {
        _listStockSelected.value = ((_listStockSelected.value ?: emptyList()) + stocks.filter { (_listStockSelected.value ?: emptyList()).find { r -> r.product == it.product } == null }).toMutableList()
    }

    fun removeStock(i: Int) {
        _listStockSelected.value = _listStockSelected.value!!.filterIndexed { index, _ -> index != i } .toMutableList()
    }

    fun addProduct (material: SurveyProduct) {
        _listProductSelected.value = ((_listProductSelected.value ?: emptyList()) + listOf(material)).toMutableList()
    }

    fun updateProduct (product: SurveyProduct, i: Int) {
        _listProductSelected.value = _listProductSelected.value!!.mapIndexed { index, item -> if (index == i) product else item }.toMutableList()
    }

    fun updateMerchandise (merchandise: Merchandise, i: Int) {
        _dataMerchandise.value = _dataMerchandise.value!!.mapIndexed { index, item -> if (index == i) merchandise else item }.toMutableList()
    }

    fun removeProduct (i: Int) {
        _listProductSelected.value = _listProductSelected.value!!.filterIndexed { index, _ -> index != i } .toMutableList()
    }
}