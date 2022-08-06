package com.delycomps.rintisa.ui.progress

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delycomps.rintisa.api.Repository
import com.delycomps.rintisa.cache.BDLocal
import com.delycomps.rintisa.model.*
import org.json.JSONObject
import java.io.File

class ProgressViewModel : ViewModel() {
    private val _listVisit2: MutableLiveData<List<Visit2>> = MutableLiveData()
    val listVisit2: LiveData<List<Visit2>> = _listVisit2

    private val _listUserSup: MutableLiveData<List<UserSup>> = MutableLiveData()
    val listUserSup: LiveData<List<UserSup>> = _listUserSup

    private val _listPointSale: MutableLiveData<List<PointSale>> = MutableLiveData()
    val listPointSale: LiveData<List<PointSale>> = _listPointSale

    private val _loading: MutableLiveData<ResGlobal> = MutableLiveData()
    val loading: LiveData<ResGlobal> = _loading

    fun getVisit2 (token: String) {
        _loading.value = ResGlobal(true, "", false)
        Repository().getVisit2(token) { isSuccess, list, _ ->
            if (isSuccess) {
                _loading.value = ResGlobal(false, "", true)
                _listVisit2.value = list
            } else {
                _loading.value = ResGlobal(false, "Hubo uin error vuelva a intentarlo", true)
            }
        }
    }

    fun getPointSale(userid: Int, token: String) {
        _loading.value = ResGlobal(false, "", true)
        Repository().getPointsSale2(token, userid) { isSuccess, result, _ ->
            if (isSuccess) {
                _loading.value = ResGlobal(false, "", true)
                _listPointSale.value = result
            } else {
                _loading.value = ResGlobal(false, "Hubo uin error vuelva a intentarlo", true)
            }
        }
    }

    fun getUserSup (token: String) {
        _loading.value = ResGlobal(true, "", false)
        Repository().getUserSup(token) { isSuccess, list, _ ->
            if (isSuccess) {
                _loading.value = ResGlobal(false, "", true)
                _listUserSup.value = list
            } else {
                _loading.value = ResGlobal(false, "Hubo uin error vuelva a intentarlo", true)
            }
        }
    }
}