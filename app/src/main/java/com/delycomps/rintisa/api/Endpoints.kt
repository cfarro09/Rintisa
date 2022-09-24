package com.delycomps.rintisa.api

import com.delycomps.rintisa.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface Endpoints {

    @POST("auth")
    fun auth(@Body body: RequestBody): Call<ResponseLogin>

    @POST("main")
    fun getClients(@Body body: RequestBody, @Header("Authorization") authHeader: String): Call<ResponseList<PointSale>>

    @POST("main")
    fun getClients2(@Body body: RequestBody, @Header("Authorization") authHeader: String): Call<ResponseList<Customer>>

    @POST("main")
    fun getMaterials(@Body body: RequestBody, @Header("Authorization") authHeader: String): Call<ResponseList<Material>>

    @POST("main")
    fun getUserFromAuditor(@Body body: RequestBody, @Header("Authorization") authHeader: String): Call<ResponseList<UserFromAuditor>>

    @POST("main")
    fun getVisit2(@Body body: RequestBody, @Header("Authorization") authHeader: String): Call<ResponseList<Visit2>>

    @POST("main")
    fun getUserSup(@Body body: RequestBody, @Header("Authorization") authHeader: String): Call<ResponseList<UserSup>>

    @POST("main/multi")
    fun mainMulti(@Body body: RequestBody, @Header("Authorization") authHeader: String): Call<ResponseMulti>

    @POST("main")
    fun execute(@Body body: RequestBody, @Header("Authorization") authHeader: String): Call<ResponseCommon>

    @Multipart
    @POST("upload")
    fun upload(@Part file: MultipartBody.Part, @Part("requestBody") type: RequestBody, @Header("Authorization") authHeader: String): Call<ResUploader>
}