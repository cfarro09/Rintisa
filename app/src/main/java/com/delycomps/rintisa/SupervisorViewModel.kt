package com.delycomps.rintisa

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delycomps.rintisa.api.Repository
import com.delycomps.rintisa.model.*
import org.json.JSONObject
import java.io.File

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

    private val _userSelected: MutableLiveData<Int> = MutableLiveData()
    val userSelected: LiveData<Int> = _userSelected

    private val _dataUsers: MutableLiveData<List<UserZyx>> = MutableLiveData()
    val dataUser: LiveData<List<UserZyx>> = _dataUsers

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

    private val _comment: MutableLiveData<String> = MutableLiveData()
    val comment: LiveData<String> = _comment

    private val _auditJson: MutableLiveData<String> = MutableLiveData()
    val auditJson: LiveData<String> = _auditJson

    //SUPERVISOR PROMOTOR
    private val _speechSCN: MutableLiveData<String> = MutableLiveData()
    val speechSCN: LiveData<String> = _speechSCN

    private val _speechRCN: MutableLiveData<String> = MutableLiveData()
    val speechRCN: LiveData<String> = _speechRCN

    private val _speechRCT: MutableLiveData<String> = MutableLiveData()
    val speechRCT: LiveData<String> = _speechRCT

    private val _speechSCT: MutableLiveData<String> = MutableLiveData()
    val speechSCT: LiveData<String> = _speechSCT

    private val _uniformJson: MutableLiveData<String> = MutableLiveData()
    val uniformJson: LiveData<String> = _uniformJson

    private val _materialJson: MutableLiveData<String> = MutableLiveData()
    val materialJson: LiveData<String> = _materialJson

    private val _statusJson: MutableLiveData<String> = MutableLiveData()
    val statusJson: LiveData<String> = _statusJson

    private val _image2: MutableLiveData<String> = MutableLiveData()
    val image2: LiveData<String> = _image2

    private val _image3: MutableLiveData<String> = MutableLiveData()
    val image3: LiveData<String> = _image3

    private val _image4: MutableLiveData<String> = MutableLiveData()
    val image4: LiveData<String> = _image4

    private val _image5: MutableLiveData<String> = MutableLiveData()
    val image5: LiveData<String> = _image5



    fun uploadWithBD(file: File, token: String) {
        _urlImageWithBD.value = ResGlobal(true, "", false)

        Repository().uploadImage(file, token) { isSuccess, result, _ ->
            if (isSuccess) {
                _urlImageWithBD.value = ResGlobal(false, result.toString(), true)
            } else {
                _urlImageWithBD.value = ResGlobal(false, "", false)
            }
        }
    }

    fun setComment(comment: String) {
        _comment.value = comment
    }

    fun setAudit(auditJson: String) {
        _auditJson.value = auditJson
    }

    fun setSpeechSCN(speechSCN: String) {
        _speechSCN.value = speechSCN
    }
    fun setSpeechRCN(speechRCN: String) {
        _speechRCN.value = speechRCN
    }
    fun setSpeechRCT(speechRCT: String) {
        _speechRCT.value = speechRCT
    }
    fun setSpeechSCT(speechSCT: String) {
        _speechSCT.value = speechSCT
    }

    fun setUniform(uniform: String) {
        _uniformJson.value = uniform
    }

    fun setMaterial(materialJson: String) {
        _materialJson.value = materialJson
    }

    fun setStatus(statusJson: String) {
        _statusJson.value = statusJson
    }

    fun setImage2(image2: String) {
        _image2.value = image2
    }
    fun setImage3(image3: String) {
        _image3.value = image3
    }
    fun setImage4(image4: String) {
        _image4.value = image4
    }
    fun setImage5(image5: String) {
        _image5.value = image5
    }


    fun setMultiInitial(data: DataSupervisor) {
        _dataMarket.value = data.markets
        _dataQuestion.value = data.questions
        _dataCheckSupPromoter.value = data.checks
        _dataUsers.value = data.users
    }

    fun getMainMultiInitial(token: String) {
        Repository().getMultiSupervisorInitial(token) { isSuccess, result, _ ->
            if (isSuccess) {
                _dataCheckSupPromoter.value = result?.checks ?: emptyList()
                _dataUsers.value = result?.users ?: emptyList()
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

    fun setUserSelected(userid: Int) {
        _userSelected.value = userid
    }

    fun initExecute() {
        _resExecute.value = ResGlobal(false, "", false)
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