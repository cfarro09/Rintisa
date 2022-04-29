package com.delycomps.myapplication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delycomps.myapplication.api.Repository
import com.delycomps.myapplication.cache.BDLocal
import com.delycomps.myapplication.model.Material
import com.delycomps.myapplication.model.PointSale
import com.delycomps.myapplication.model.ResGlobal
import java.io.File

class MainViewModel : ViewModel() {

    private val _listPointSale: MutableLiveData<List<PointSale>> = MutableLiveData()
    val listPointSale: LiveData<List<PointSale>> = _listPointSale

    private val _errorOnGetList: MutableLiveData<String> = MutableLiveData()
    val errorOnGetList: LiveData<String> = _errorOnGetList

    private val _loadingSelfie: MutableLiveData<Boolean> = MutableLiveData()
    val loadingSelfie: LiveData<Boolean> = _loadingSelfie

    private val _urlSelfie: MutableLiveData<String> = MutableLiveData()
    val urlSelfie: LiveData<String> = _urlSelfie

    private val _sendInitPointSale: MutableLiveData<Boolean> = MutableLiveData()
    val sendInitPointSale: LiveData<Boolean> = _sendInitPointSale

    private val _gpsEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val gpsEnabled: LiveData<Boolean> = _gpsEnabled

    private val _resSaveAssistance: MutableLiveData<ResGlobal> = MutableLiveData()
    val resSaveAssistance: LiveData<ResGlobal> = _resSaveAssistance

    fun initPointSale(token: String, visitId: Int, photo_selfie: String, latitude: Double, longitude: Double) {
        Repository().insInitPointSale(visitId, photo_selfie, latitude, longitude, token) { isSuccess, _ ->
            _sendInitPointSale.value = isSuccess
        }
    }

    fun getListLocation(context: Context, token: String) {
        Repository().getPointsSale(token) { isSuccess, result, message ->
            if (isSuccess) {
                _listPointSale.value = result!!
                BDLocal(context).savePointSales(result)
            } else {
                _errorOnGetList.value = message
                val aux = BDLocal(context).getPointSale()
                _listPointSale.value = aux
            }
        }
    }

    fun uploadSelfie(file: File, token: String) {
        _loadingSelfie.value = true
        Repository().uploadImage(file, token) { isSuccess, result, _ ->
            if (isSuccess) {
                _urlSelfie.value = result!!
            } else {
                _urlSelfie.value = ""
            }
            _loadingSelfie.value = false
        }
    }
    fun saveAssistance(latitude: Double, longitude: Double,token: String) {
        _resSaveAssistance.value = ResGlobal(true, "", false)
        Repository().saveAttendance(latitude, longitude, token) { isSuccess, _ ->
            if (isSuccess) {
                _resSaveAssistance.value = ResGlobal(false, "", true)
            }
            _resSaveAssistance.value = ResGlobal(false, "", false)
        }
    }
    fun setGPSIsEnabled(enabled: Boolean) {
        _gpsEnabled.value = enabled
    }
}