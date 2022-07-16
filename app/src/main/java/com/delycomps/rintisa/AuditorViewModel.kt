package com.delycomps.rintisa

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delycomps.rintisa.api.Repository
import com.delycomps.rintisa.model.*
import org.json.JSONObject

class AuditorViewModel : ViewModel() {
    private val _listCustomer: MutableLiveData<List<Customer>> = MutableLiveData()
    val listCustomer: LiveData<List<Customer>> = _listCustomer

    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading

    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    private val _dataCheckSupPromoter: MutableLiveData<List<CheckSupPromoter>> = MutableLiveData()
    val dataCheckSupPromoter: LiveData<List<CheckSupPromoter>> = _dataCheckSupPromoter

    private val _dataMarket: MutableLiveData<List<Market>> = MutableLiveData()
    val dataMarket: LiveData<List<Market>> = _dataMarket

    private val _resExecute: MutableLiveData<ResGlobal> = MutableLiveData()
    val resExecute: LiveData<ResGlobal> = _resExecute


    fun setMultiInitial(data: DataAuditor) {
        _dataCheckSupPromoter.value = data.checks
        _dataMarket.value = data.markets

    }

    fun getMainMultiInitial(token: String) {
        Repository().getMultiAuditorInitial(token) { isSuccess, result, _ ->
            if (isSuccess) {
                _dataCheckSupPromoter.value = result?.checks ?: emptyList()
                _dataMarket.value = result?.markets ?: emptyList()
            }
        }
    }

    fun executeSupervisor(jo: JSONObject, method: String, token: String) {
        _resExecute.value = ResGlobal(true, method, false)
        Repository().executeSupervisor(jo, method, token) { isSuccess, _ ->
            if (isSuccess) {
                _resExecute.value = ResGlobal(false, method, true)
            } else {
                _resExecute.value = ResGlobal(false, method, false)
            }
        }
    }

    fun initExecute() {
        _resExecute.value = ResGlobal(false, "REGISTRADO", false)
    }

    fun getCustomer(marketId: Int, token: String) {
        _loading.value = true
        Repository().getCustomer(token, marketId) { isSuccess, result, message ->
            _loading.value = false
            if (isSuccess) {
                _listCustomer.value = result!!
            } else {
                _error.value = message
                _listCustomer.value = emptyList()
            }
        }
    }
}