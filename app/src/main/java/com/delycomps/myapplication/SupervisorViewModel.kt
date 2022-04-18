package com.delycomps.myapplication

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delycomps.myapplication.api.Repository
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.model.*
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SupervisorViewModel : ViewModel() {
    private val _listPointSale: MutableLiveData<List<PointSale>> = MutableLiveData()
    val listPointSale: LiveData<List<PointSale>> = _listPointSale

    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading

    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    private val _dataMarket: MutableLiveData<List<Market>> = MutableLiveData()
    val dataMarket: LiveData<List<Market>> = _dataMarket

    fun setMultiInitial(data: DataSupervisor) {
        _dataMarket.value = data.markets
    }

    fun getMainMultiInitial(token: String) {
        Repository().getMultiSupervisorInitial(token) { isSuccess, result, _ ->
            if (isSuccess) {
                _dataMarket.value = result?.markets ?: emptyList()
            }
        }
    }

    fun getListLocation(marketId: Int, service: String, token: String) {
        _loading.value = true
        Repository().getPointsSale(token, true, marketId,  service) { isSuccess, result, message ->
            _loading.value = false
            if (isSuccess) {
                _listPointSale.value = result!!
            } else {
                _error.value = message
                _listPointSale.value = emptyList()
            }
        }
    }
}