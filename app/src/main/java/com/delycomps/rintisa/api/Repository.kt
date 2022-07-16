package com.delycomps.rintisa.api

import com.delycomps.rintisa.model.*
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
        supervisor: Boolean = false,
        marketId: Int = 0,
        service: String = "",
        switchDayVisit: Boolean = false,
        onResult: (isSuccess: Boolean, result: List<PointSale>?, message: String?) -> Unit
    ) {
        val method = if (supervisor)  "UFN_CUSTOMER_BY_SUPERVISOR1" else "UFN_CUSTOMER_BY_USER_SEL"
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(RequestBodyX(method, method, mapOf<String, Any>(
                "marketid" to marketId,
                "service" to service,
                "today" to switchDayVisit,
            )))
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

    fun getCustomer(
        token: String,
        marketId: Int = 0,
        onResult: (isSuccess: Boolean, result: List<Customer>?, message: String?) -> Unit
    ) {
        val method = "UFN_CUSTOMER_BY_AUDITOR"
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(RequestBodyX(method, method, mapOf<String, Any>(
                "marketid" to marketId
            )))
        )
        try {
            Connection.instance.getClients2(body, "Bearer $token").enqueue(object :
                Callback<ResponseList<Customer>> {
                override fun onResponse(
                    call: Call<ResponseList<Customer>>?,
                    response: Response<ResponseList<Customer>>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, response.body()!!.data, null)
                    } else {
                        onResult(false, null, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseList<Customer>>?, t: Throwable?) {
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

    fun executeSupervisor(
        jo: JSONObject,
        method: String,
        token: String,
        onResult: (isSuccess: Boolean, message: String?) -> Unit
    )  {
        val jo1 = JSONObject()
        jo1.put("method", method)
        jo1.put("parameters", jo)

        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jo1.toString()
        )
        try {
            Connection.instance.execute(body, "Bearer $token").enqueue(object :
                Callback<ResponseCommon> {
                override fun onResponse(
                    call: Call<ResponseCommon>?,
                    response: Response<ResponseCommon>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, method)
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
        availability_survey_list: String,
        haveAvailability: Boolean,
        status_management: String,
        motive: String,
        observation: String,
        token: String,
        dateFinish: String?,
        dateStart: String? = "",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        onResult: (isSuccess: Boolean, message: String?) -> Unit
    )  {
        val method = if (dateFinish != null) "UFN_UPLOAD_IMAGE_AFTER_VISIT_FINISH_X" else "UFN_UPLOAD_IMAGE_AFTER_VISIT3_X"

        val aa = Gson().toJson(RequestBodyX(method, method, mapOf<String, Any>(
            "visitid" to visitId,
            "image_before" to image_before,
            "image_after" to image_after,
            "material_list" to material_list,
            "pricesurvey_list" to price_survey_list,
            "havesurvey" to haveSurvey,
            "availability_survey_list" to availability_survey_list,
            "haveavailability" to haveAvailability,
            "status_manage" to status_management,
            "motive_visit" to motive,
            "observations" to observation,
            "finish_date_visit" to (dateFinish ?: ""),
            "start_date_visit" to (dateStart ?: ""),
            "latitude_start" to (latitude),
            "longitude_start" to (longitude),
        )))

        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            aa
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
//    QUERY_MATERIALS_SEL

    fun getMaterials(
        visitId: Int,
        token: String,
        onResult: (isSuccess: Boolean, result: List<Material>?, message: String?) -> Unit
    )  {
        val rb = RequestBodyX("QUERY_MATERIALS_SEL", "QUERY_MATERIALS_SEL", mapOf<String, Any>("visitid" to visitId))

        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(rb)
        )

        try {
            Connection.instance.getMaterials(body, "Bearer $token").enqueue(object :
                Callback<ResponseList<Material>> {
                override fun onResponse(
                    call: Call<ResponseList<Material>>?,
                    response: Response<ResponseList<Material>>?
                ) {
                    if (response!!.isSuccessful) {
                        onResult(true, response.body()!!.data, null)
                    } else {
                        onResult(false, null, DEFAULT_MESSAGE_ERROR)
                    }
                }
                override fun onFailure(call: Call<ResponseList<Material>>?, t: Throwable?) {
                    onResult(false, null, DEFAULT_MESSAGE_ERROR)
                }
            })
        } catch (e: java.lang.Exception){
            onResult(false, null, DEFAULT_MESSAGE_ERROR)
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
        latitude: Double,
        longitude: Double,
        token: String,
        onResult: (isSuccess: Boolean, message: String?) -> Unit
    )  {
        val jsonto = Gson().toJson(RequestBodyX("UFN_ASSISTANCE_REPORT_INS2", "UFN_ASSISTANCE_REPORT_INS2", mapOf<String, Any>(
            "id" to 0,
            "type" to "NINGUNO",
            "status" to "ACTIVO",
            "lat" to latitude,
            "lon" to longitude,
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
        dateFinish: String?,
        dateStart: String? = "",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        onResult: (isSuccess: Boolean, message: String?) -> Unit
    )  {
        val method = if (dateFinish != null) "UFN_UPDATE_REPLACE_STOCK_SALE_VISIT_FINISH_X" else "UFN_UPDATE_REPLACE_STOCK_SALE_VISIT_X"

        val jsonto = Gson().toJson(RequestBodyX(method, method, mapOf<String, Any>(
            "visitid" to visitId,
            "replace_stock" to stock_list,
            "sale_list" to sales_list,
            "have" to showSale,
            "merchandising" to merchandises,
            "finish_date_visit" to (dateFinish ?: ""),
            "start_date_visit" to (dateStart ?: ""),
            "latitude_start" to (latitude),
            "longitude_start" to (longitude),
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
            RequestBodyX("UFN_PRODUCT_COMPETENCE_SEL", "UFN_PRODUCT_COMPETENCE_SEL", mapOf<String, Any>(
                "id" to 0,
                "all" to true,
                "competence" to ""
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
                            resultMerchant.products = response.body().data[2].data.toList().map { r -> SurveyProduct(
                                r["productid"].toString().toDouble().toInt(), r["description"].toString(), r["brand"].toString(), 0.00, "", 0.0, null, null, UUID.randomUUID().toString(), r["competence"].toString()) }
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
                                            r["quantity"].toString().toDouble(),
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

    fun getMultiSupervisorInitial(
        token: String,
        onResult: (isSuccess: Boolean, result: DataSupervisor?, message: String?) -> Unit
    )  {
        val method = "QUERY_MARKET_SEL"
        val multi = listOf(
            RequestBodyX(method, method, mapOf<String, Any>()),
            RequestBodyX("UFN_DOMAIN_LST_VALORES", "UFN_DOMAIN_LST_VALORES", mapOf<String, Any>("domainname" to "QUESTIONMERCHANT")),
            RequestBodyX("UFN_DOMAIN_LST_VALORES", "UFN_DOMAIN_LST_VALORES", mapOf<String, Any>("domainname" to "CHECK_SUPERVISOR_PROMOTER")),
            RequestBodyX("QUERY_USER_PROMOTER_LST", "QUERY_USER_PROMOTER_LST", mapOf<String, Any>()),
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
                        val dataSupervisor = DataSupervisor(emptyList(), emptyList(), emptyList(), emptyList())

                        if (response.body().data[0].success == true) {
                            dataSupervisor.markets = response.body().data[0].data.toList().map { Market("(" + it["marketid"].toString() + ") " + it["description"].toString(), it["marketid"].toString().toDouble().toInt()) }
                        }
                        if (response.body().data[1].success == true) {
                            dataSupervisor.questions = response.body().data[1].data.toList().map { Question(it["domainvalue"].toString(), it["domaindesc"].toString(), false) }
                        }
                        if (response.body().data[2].success == true) {
                            dataSupervisor.checks = response.body().data[2].data.toList().map { CheckSupPromoter(it["domainvalue"].toString(), it["domaindesc"].toString(), it["type"].toString(), false) }
                        }
                        if (response.body().data[3].success == true) {
                            dataSupervisor.users = response.body().data[3].data.toList().map { UserZyx(it["userid"].toString().toDouble().toInt(), it["description"].toString()) }
                        }
                        onResult(true, dataSupervisor, null)
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

    fun getMultiAuditorInitial(
        token: String,
        onResult: (isSuccess: Boolean, result: DataAuditor?, message: String?) -> Unit
    )  {
        val method = "QUERY_MARKET_SEL"
        val multi = listOf(
            RequestBodyX(method, method, mapOf<String, Any>()),
            RequestBodyX("UFN_DOMAIN_LST_VALORES", "UFN_DOMAIN_LST_VALORES", mapOf<String, Any>("domainname" to "QUESTION_AUDITOR")),
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
                        val dataAuditor = DataAuditor(emptyList(), emptyList())

                        if (response.body().data[0].success == true) {
                            dataAuditor.markets = response.body().data[0].data.toList().map { Market("(" + it["marketid"].toString() + ") " + it["description"].toString(), it["marketid"].toString().toDouble().toInt()) }
                        }
                        if (response.body().data[1].success == true) {
                            dataAuditor.checks = response.body().data[1].data.toList().map { CheckSupPromoter(it["domainvalue"].toString(), it["domaindesc"].toString(), it["type"].toString(), false) }
                        }
                        onResult(true, dataAuditor, null)
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