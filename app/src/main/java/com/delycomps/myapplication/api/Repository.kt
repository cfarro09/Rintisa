package com.delycomps.myapplication.api

import android.util.Log
import com.delycomps.myapplication.model.*
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

const val DEFAULT_MESSAGE_ERROR = "Hubo un error vuelva a intentarlo, o vuelva a iniciar sesión"

class Repository {

    fun login(
        username: String,
        password: String,
        onResult: (isSuccess: Boolean, result: User?, message: String?) -> Unit
    )  {
        val oo = JSONObject()
        oo.put("usr", username)
        oo.put("password", password)
        oo.put("origin", "MOBIL")

        val data = JSONObject()
        data.put("data", oo)

        val body: RequestBody = RequestBody.create(MediaType.parse("application/json"), data.toString())

        try {
            Connection.instance.auth(body).enqueue(object : Callback<ResponseLogin> {
                override fun onResponse(
                    call: Call<ResponseLogin>?,
                    response: Response<ResponseLogin>?
                ) {
                    if (response!!.isSuccessful) {
                        val user: User = response.body()!!.data!!
                        onResult(true, user, null)
                    } else {
                        onResult(false, null, "Usuario incorrecto")
                    }
                }

                override fun onFailure(call: Call<ResponseLogin>?, t: Throwable?) {
                    onResult(false, null, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, null, DEFAULT_MESSAGE_ERROR)
        }
    }

    fun getPointsSale(
        token: String,
        onResult: (isSuccess: Boolean, result: List<PointSale>?, message: String?) -> Unit
    )  {
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(RequestBodyX("UFN_CUSTOMER_BY_USER_SEL", "UFN_CUSTOMER_BY_USER_SEL", mapOf<String, Any>()))
        )
        try {
            Connection.instance.getClients(body, "Bearer $token").enqueue(object :
                Callback<ResponseList<PointSale>> {
                override fun onResponse(
                    call: Call<ResponseList<PointSale>>?,
                    response: Response<ResponseList<PointSale>>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, response.body()!!.data, null)
                    } else {
                        onResult(false, null, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseList<PointSale>>?, t: Throwable?) {
                    onResult(false, null, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, null, DEFAULT_MESSAGE_ERROR)
        }
    }

    fun insInitPointSale(
        visitId: Int,
        photo_selfie: String,
        latitude: Double,
        longitude: Double,
        token: String,
        onResult: (isSuccess: Boolean, message: String?) -> Unit
    )  {
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(RequestBodyX("UFN_UPLOAD_SELFIE_VISIT", "UFN_UPLOAD_SELFIE_VISIT", mapOf<String, Any>(
                "visitid" to visitId,
                "photo_selfie" to photo_selfie,
                "latitude" to latitude,
                "longitude" to longitude,
            )))
        )
        try {
            Connection.instance.execute(body, "Bearer $token").enqueue(object :
                Callback<ResponseCommon> {
                override fun onResponse(
                    call: Call<ResponseCommon>?,
                    response: Response<ResponseCommon>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseCommon>?, t: Throwable?) {
                    onResult(false, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, DEFAULT_MESSAGE_ERROR)
        }
    }

    fun insCloseManageMerchant(
        visitId: Int,
        image_before: String,
        image_after: String,
        material_list: String,
        price_survey_list: String,
        haveSurvey: Boolean,
        token: String,
        onResult: (isSuccess: Boolean, message: String?) -> Unit
    )  {
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(RequestBodyX("UFN_UPLOAD_IMAGE_AFTER_VISIT", "UFN_UPLOAD_IMAGE_AFTER_VISIT", mapOf<String, Any>(
                "visitid" to visitId,
                "image_before" to image_before,
                "image_after" to image_after,
                "material_list" to material_list,
                "pricesurvey_list" to price_survey_list,
                "havesurvey" to haveSurvey,
            )))
        )
        try {
            Connection.instance.execute(body, "Bearer $token").enqueue(object :
                Callback<ResponseCommon> {
                override fun onResponse(
                    call: Call<ResponseCommon>?,
                    response: Response<ResponseCommon>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseCommon>?, t: Throwable?) {
                    onResult(false, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, DEFAULT_MESSAGE_ERROR)
        }
    }


    fun updatePromoter(
        visitId: Int,
        method: String,
        json: String,
        key: String, //replace_stock
        token: String,
        onResult: (isSuccess: Boolean, message: String?) -> Unit
    )  {
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),

            Gson().toJson(RequestBodyX(method, method, mapOf<String, Any>(
                "visitid" to visitId,
                key to json
            )))
        )
        try {
            Connection.instance.execute(body, "Bearer $token").enqueue(object :
                Callback<ResponseCommon> {
                override fun onResponse(
                    call: Call<ResponseCommon>?,
                    response: Response<ResponseCommon>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseCommon>?, t: Throwable?) {
                    onResult(false, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, DEFAULT_MESSAGE_ERROR)
        }
    }


    fun saveAttendance(
        token: String,
        onResult: (isSuccess: Boolean, message: String?) -> Unit
    )  {
        val jsonto = Gson().toJson(RequestBodyX("UFN_ASSISTANCE_REPORT_INS", "UFN_ASSISTANCE_REPORT_INS", mapOf<String, Any>(
            "id" to 0,
            "type" to "NINGUNO",
            "status" to "ACTIVO",
            "description" to "",
            "operation" to "INSERT",
        )))

        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonto
        )
        try {
            Connection.instance.execute(body, "Bearer $token").enqueue(object :
                Callback<ResponseCommon> {
                override fun onResponse(
                    call: Call<ResponseCommon>?,
                    response: Response<ResponseCommon>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseCommon>?, t: Throwable?) {
                    onResult(false, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, DEFAULT_MESSAGE_ERROR)
        }
    }

    fun insCloseManagePromoter(
        visitId: Int,
        stock_list: String,
        sales_list: String,
        showSale: Boolean,
        merchandises: String,
        token: String,
        onResult: (isSuccess: Boolean, message: String?) -> Unit
    )  {
        val jsonto = Gson().toJson(RequestBodyX("UFN_UPDATE_REPLACE_STOCK_SALE_VISIT", "UFN_UPDATE_REPLACE_STOCK_SALE_VISIT", mapOf<String, Any>(
            "visitid" to visitId,
            "replace_stock" to stock_list,
            "sale_list" to sales_list,
            "have" to showSale,
            "merchandising" to merchandises,
        )))
        Log.d("log_carlos_json", jsonto)
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonto
        )
        try {
            Connection.instance.execute(body, "Bearer $token").enqueue(object :
                Callback<ResponseCommon> {
                override fun onResponse(
                    call: Call<ResponseCommon>?,
                    response: Response<ResponseCommon>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseCommon>?, t: Throwable?) {
                    onResult(false, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, DEFAULT_MESSAGE_ERROR)
        }
    }

    fun insCloseIncompleteManage(
        visitId: Int,
        observation: String,
        token: String,
        onResult: (isSuccess: Boolean, message: String?) -> Unit
    )  {
        val jsonto = Gson().toJson(RequestBodyX("UFN_UFN_CLOSE_MANAGE_VISIT", "UFN_UFN_CLOSE_MANAGE_VISIT", mapOf<String, Any>(
            "visitid" to visitId,
            "observation" to observation
        )))
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonto
        )
        try {
            Connection.instance.execute(body, "Bearer $token").enqueue(object :
                Callback<ResponseCommon> {
                override fun onResponse(
                    call: Call<ResponseCommon>?,
                    response: Response<ResponseCommon>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseCommon>?, t: Throwable?) {
                    onResult(false, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, DEFAULT_MESSAGE_ERROR)
        }
    }

    fun getMultiMerchant(
        token: String,
        onResult: (isSuccess: Boolean, result: DataMerchant?, message: String?) -> Unit
    )  {
        val multi = listOf(
            RequestBodyX("UFN_DOMAIN_LST_VALORES", "UFN_DOMAIN_LST_VALORES", mapOf<String, Any>("domainname" to "MARCA")),
            RequestBodyX("UFN_DOMAIN_LST_VALORES", "UFN_DOMAIN_LST_VALORES", mapOf<String, Any>("domainname" to "MATERIALPOP")),
            RequestBodyX("UFN_DOMAIN_LST_VALORES", "UFN_PRODUCT_COMPETENCE_SEL", mapOf<String, Any>(
                "id" to 0,
                "all" to true,
                "competence" to "RINTI"
            )),
        )
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(multi)
        )
        try {
            Connection.instance.mainMulti(body, "Bearer $token").enqueue(object :
                Callback<ResponseMulti> {
                override fun onResponse(
                    call: Call<ResponseMulti>?,
                    response: Response<ResponseMulti>?
                ) {
                    if (response?.isSuccessful == true && response.body().success == true) {
                        val resultMerchant = DataMerchant(emptyList(), emptyList(), emptyList())

                        if (response.body().data[0].success == true) {
                            resultMerchant.brands = response.body().data[0].data.toList().map { r -> r["domainvalue"].toString() }
                        }
                        if (response.body().data[1].success == true) {
                            resultMerchant.materials = response.body().data[1].data.toList().map { r -> Material(r["domainvalue"].toString(),"", 0) }
                        }
                        if (response.body().data[2].success == true) {
                            resultMerchant.products = response.body().data[2].data.toList().map { r -> SurveyProduct(r["productid"].toString().toDouble().toInt(), r["description"].toString(), r["brand"].toString(), 0.00, "", 0) }
                        }
                        onResult(true, resultMerchant, null)
                    } else {
                        onResult(false, null, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseMulti>?, t: Throwable?) {
                    onResult(false, null, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, null, DEFAULT_MESSAGE_ERROR)
        }
    }


    fun getMultiPromoter(
        visitid: Int,
        token: String,
        onResult: (isSuccess: Boolean, result: DataPromoter?, message: String?) -> Unit
    )  {
        val multi = listOf(
            RequestBodyX("UFN_STOCK_SALE_SEL", "UFN_STOCK_SALE_SEL", mapOf<String, Any>("visitid" to visitid)),
        )
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(multi)
        )
        try {
            Connection.instance.mainMulti(body, "Bearer $token").enqueue(object :
                Callback<ResponseMulti> {
                override fun onResponse(
                    call: Call<ResponseMulti>?,
                    response: Response<ResponseMulti>?
                ) {
                    if (response?.isSuccessful == true && response.body().success == true) {
                        val dataPromoter = DataPromoter(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
                        if (response.body().data[0].success == true) {
                            val listRes = response.body().data[0].data.toList()
                            if (listRes.count() > 0) {
                                val stocks = listRes[0]["replace_stock"]
                                val sales = listRes[0]["sale"]
                                if (stocks != null) {
                                    val stocks2 = stocks as List<Map<String, Any>>
                                    dataPromoter.stocksSelected = stocks2.map { r ->
                                        Stock(
                                            r["category"].toString(),
                                            r["brand"].toString(),
                                            r["product"].toString()
                                        )
                                    }
                                }
                                if (sales != null) {
                                    val sales2 = sales as List<Map<String, Any>>
                                    dataPromoter.productsSelected = sales2.map { r ->
                                        val productId = r["productid"]?.toString()?.toDouble()?.toInt() ?: 0
                                        SurveyProduct(
                                            productId,
                                            r["saledetail_description"].toString(),
                                            r["saledetail_description"].toString(),
                                            0.00,
                                            r["measure_unit"].toString(),
                                            r["quantity"].toString().toDouble().toInt(),
                                            r["merchant"].toString(),
                                            r["url_evidence"].toString(),
                                        )
                                    }
                                }
                            }
                        }
                        onResult(true, dataPromoter, null)
                    } else {
                        onResult(false, null, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseMulti>?, t: Throwable?) {
                    onResult(false, null, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, null, DEFAULT_MESSAGE_ERROR)
        }
    }

    fun getMultiPromoterInitial(
        token: String,
        onResult: (isSuccess: Boolean, result: DataPromoter?, message: String?) -> Unit
    )  {
        val multi = listOf(
            RequestBodyX("UFN_DOMAIN_LST_VALORES", "UFN_DOMAIN_LST_VALORES", mapOf<String, Any>("domainname" to "MERCHANDISING")),
            RequestBodyX("UFN_DOMAIN_LST_VALORES", "UFN_DOMAIN_LST_VALORES", mapOf<String, Any>("domainname" to "MARCAVENTAS")),
            RequestBodyX("UFN_DOMAIN_LST_VALORES", "UFN_DOMAIN_LST_VALORES", mapOf<String, Any>("domainname" to "MARCAPRODUCTO")),
        )
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(multi)
        )
        try {
            Connection.instance.mainMulti(body, "Bearer $token").enqueue(object :
                Callback<ResponseMulti> {
                override fun onResponse(
                    call: Call<ResponseMulti>?,
                    response: Response<ResponseMulti>?
                ) {
                    if (response?.isSuccessful == true && response.body().success == true) {
                        val dataPromoter = DataPromoter(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())

                        if (response.body().data[0].success == true) {
                            dataPromoter.merchandises = response.body().data[0].data.toList().map { Merchandise(it["domaindesc"].toString()) }
                        }
                        if (response.body().data[1].success == true) {
                            dataPromoter.saleBrand = response.body().data[1].data.toList().map { r -> BrandSale(r["domainvalue"].toString(), r["domaindesc"].toString().split(",").toList()) }
                        }
                        if (response.body().data[2].success == true) {
                            dataPromoter.stocks = response.body().data[2].data.toList().map { r -> Stock(r["type"].toString(), r["domaindesc"].toString(), r["domainvalue"].toString()) }
                        }
                        onResult(true, dataPromoter, null)
                    } else {
                        onResult(false, null, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseMulti>?, t: Throwable?) {
                    onResult(false, null, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, null, DEFAULT_MESSAGE_ERROR)
        }
    }


    fun uploadImage (
        file: File,
        token: String,
        rb: String? = "",
        onResult: (isSuccess: Boolean, result: String?, message: String?) -> Unit
    )  {
        val fileReqBody: RequestBody = RequestBody.create(MediaType.parse("*/*"), file)
        val part: MultipartBody.Part = MultipartBody.Part.createFormData("file", file.name, fileReqBody)

        try {
            val rb: RequestBody = RequestBody.create(MediaType.parse("text/plain"), rb)
            Connection.instance.upload(part, rb,"Bearer $token").enqueue(object :
                Callback<ResUploader> {
                override fun onResponse(
                    call: Call<ResUploader>?,
                    response: Response<ResUploader>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, response.body().url, null)
                    } else {
                        onResult(false, null, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResUploader>?, t: Throwable?) {
                    onResult(false, null, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, null, DEFAULT_MESSAGE_ERROR)
        }
    }
}