package com.delycomps.rintisa

import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delycomps.rintisa.api.Repository
import com.delycomps.rintisa.model.*
import org.json.JSONObject
import java.io.File

class AuditorViewModel : ViewModel() {
    private val _listCustomer: MutableLiveData<List<Customer>> = MutableLiveData()
    val listCustomer: LiveData<List<Customer>> = _listCustomer

    private val _listUser: MutableLiveData<List<UserFromAuditor>> = MutableLiveData()
    val listUser: LiveData<List<UserFromAuditor>> = _listUser

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

    private val _questionClients: MutableLiveData<List<CheckSupPromoter>> = MutableLiveData()
    val questionClients: LiveData<List<CheckSupPromoter>> = _questionClients

    private val _questionUsers: MutableLiveData<List<CheckSupPromoter>> = MutableLiveData()
    val questionUsers: LiveData<List<CheckSupPromoter>> = _questionUsers

    private val _image1: MutableLiveData<String> = MutableLiveData()
    val image1: LiveData<String> = _image1

    private val _image2: MutableLiveData<String> = MutableLiveData()
    val image2: LiveData<String> = _image2

    private val _image3: MutableLiveData<String> = MutableLiveData()
    val image3: LiveData<String> = _image3

    private val _image4: MutableLiveData<String> = MutableLiveData()
    val image4: LiveData<String> = _image4

    fun uploadImage(file: File, numberImage: String, token: String) {
        _loading.value = true
        Repository().uploadImage(file, token) { isSuccess, result, _ ->
            when (numberImage) {
                "1" -> {
                    _image1.value = if (isSuccess) result!! else ""
                }
                "2" -> {
                    _image2.value = if (isSuccess) result!! else ""
                }
                "3" -> {
                    _image3.value = if (isSuccess) result!! else ""
                }
                "4" -> {
                    _image4.value = if (isSuccess) result!! else ""
                }
            }
            _loading.value = false
        }
    }

    fun setMultiInitial(data: DataAuditor) {
        _dataCheckSupPromoter.value = data.checks
        _dataMarket.value = data.markets
    }

    fun setMultiInitialRinti(data: DataAuditorRinti) {
        _questionClients.value = data.clients
        _questionUsers.value = data.users
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

    fun getMainMultiRintiInitial(token: String) {
        Repository().getMultiAuditorRintiInitial(token) { isSuccess, result, _ ->
            if (isSuccess) {
                _questionClients.value = result?.clients ?: emptyList()
                _dataMarket.value = result?.markets ?: emptyList()
                _questionUsers.value = result?.users ?: emptyList()
            }
        }
    }

    fun executeSupervisor(jo: JSONObject, method: String, token: String) {
        _loading.value = true
        _resExecute.value = ResGlobal(true, method, false)
        Repository().executeSupervisor(jo, method, token) { isSuccess, _ ->
            _loading.value = false
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

    fun getCustomer(marketId: Int, isRinti: Boolean, token: String) {
        _loading.value = true
        Repository().getCustomer(token, marketId, isRinti) { isSuccess, result, message ->
            _loading.value = false
            if (isSuccess) {
                _listCustomer.value = result!!
            } else {
                _error.value = message
                _listCustomer.value = emptyList()
            }
        }
    }

    fun getUser(role: String, token: String) {
        _loading.value = true
        Repository().getUserFromAuditorRinti(role, token) { isSuccess, result, message ->
            _loading.value = false
            if (isSuccess) {
                _listUser.value = result!!
            } else {
                _error.value = message
                _listUser.value = emptyList()
            }
        }
    }
}