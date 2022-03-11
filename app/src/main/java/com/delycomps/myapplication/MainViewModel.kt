package com.delycomps.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delycomps.myapplication.api.Repository
import com.delycomps.myapplication.model.Material
import com.delycomps.myapplication.model.PointSale
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

    fun initPointSale(token: String, visitId: Int, photo_selfie: String, latitude: Double, longitude: Double) {
        Repository().insInitPointSale(visitId, photo_selfie, latitude, longitude, token) { isSuccess, _ ->
            _sendInitPointSale.value = isSuccess
        }
    }

    fun getListLocation(token: String) {
        Repository().getPointsSale(token) { isSuccess, result, message ->
            if (isSuccess) {
                _listPointSale.value = result!!
            } else {
                _errorOnGetList.value = message
                _listPointSale.value = emptyList()
            }
        }
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
    fun setGPSIsEnabled(enabled: Boolean) {
        _gpsEnabled.value = enabled
    }
}