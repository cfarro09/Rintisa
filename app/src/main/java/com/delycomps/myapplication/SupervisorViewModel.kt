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
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SupervisorViewModel : ViewModel() {
    private val _listPointSale: MutableLiveData<List<PointSale>> = MutableLiveData()
    val listPointSale: LiveData<List<PointSale>> = _listPointSale

    private val _listMaterial: MutableLiveData<List<Material>> = MutableLiveData()
    val listMaterial: LiveData<List<Material>> = _listMaterial

    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading

    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    private val _dataMarket: MutableLiveData<List<Market>> = MutableLiveData()
    val dataMarket: LiveData<List<Market>> = _dataMarket

    private val _dataQuestion: MutableLiveData<List<Question>> = MutableLiveData()
    val dataQuestion: LiveData<List<Question>> = _dataQuestion

    private val _dataCheckSupPromoter: MutableLiveData<List<CheckSupPromoter>> = MutableLiveData()
    val dataCheckSupPromoter: LiveData<List<CheckSupPromoter>> = _dataCheckSupPromoter

    private val _questionAnswered: MutableLiveData<List<Question>> = MutableLiveData()
    val questionAnswered: LiveData<List<Question>> = _questionAnswered

    private val _resExecute: MutableLiveData<ResGlobal> = MutableLiveData()
    val resExecute: LiveData<ResGlobal> = _resExecute

    private val _urlImageWithBD: MutableLiveData<ResGlobal> = MutableLiveData()
    val urlImageWithBD: LiveData<ResGlobal> = _urlImageWithBD

    fun uploadWithBD(file: File, json: String, token: String) {
        _urlImageWithBD.value = ResGlobal(true, "", false)

        Repository().uploadImage(file, token, json) { isSuccess, result, _ ->
            if (isSuccess) {
                _urlImageWithBD.value = ResGlobal(false, result.toString(), true)
            } else {
                _urlImageWithBD.value = ResGlobal(false, "", false)
            }
        }
    }

    fun setMultiInitial(data: DataSupervisor) {
        _dataMarket.value = data.markets
        _dataQuestion.value = data.questions
        _dataCheckSupPromoter.value = data.checks
    }

    fun getMainMultiInitial(token: String) {
        Repository().getMultiSupervisorInitial(token) { isSuccess, result, _ ->
            if (isSuccess) {
                _dataCheckSupPromoter.value = result?.checks ?: emptyList()
                _dataQuestion.value = result?.questions ?: emptyList()
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
        _resExecute.value = ResGlobal(false, "", false)
    }

    fun manageQuestion (question: Question, context: Context, customerId: Int) {
//        BDLocal(context).addMaterialStock(material, visitId)
        if (question.flag) {
            _questionAnswered.value = ((_questionAnswered.value ?: emptyList()) + listOf(question)).toMutableList()
//            BDLocal(context).addProductsAvailability(question, visitId)
        } else {
//            val uuid = _questionAnswered.value?.find { it.text == question.text }?.uuid ?: ""
//            BDLocal(context).deleteProductAvailability(uuid)
            _questionAnswered.value = _questionAnswered.value!!.filter { it.text != question.text }.toMutableList()
        }
    }

    fun getMaterials(visitId: Int, token: String) {
        Repository().getMaterials(visitId, token) { isSuccess, result, _ ->
            if (isSuccess) {
                _listMaterial.value = result ?: emptyList()
            }
        }
    }

    fun getListLocation(marketId: Int, service: String, switchDayVisit: Boolean, token: String) {
        _loading.value = true
        Repository().getPointsSale(token, true, marketId,  service, switchDayVisit) { isSuccess, result, message ->
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