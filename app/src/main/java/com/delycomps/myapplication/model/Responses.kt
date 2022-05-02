package com.delycomps.myapplication.model

import java.util.*

data class ResponseLogin(
    var success: Boolean,
    var data: User?,
)

data class ResponseList<T>(
    var success: Boolean?,
    var error: Boolean? = false,
    var key: String?,
    var data: List<T>,
)

data class ResponseCommon (
    var success: Boolean?,
    var error: Boolean? = false,
    var key: String?,
)

data class Availability(
    var productid: Int,
    var description: String,
    var brand: String,
    var competence: String,
    var flag: Boolean?,
    var uuid: String? = UUID.randomUUID().toString(),
    var customerid: Int? = 0
)


data class ResponseMulti(
    var success: Boolean?,
    var data: List<ResponseList<Map<String, Any>>>,
)

data class ResGlobal(
    var loading: Boolean,
    var result: String,
    var success: Boolean
)

data class Management(
    var status_management: String?,
    var motive: String?,
    var observation: String?
)

data class PriceProduct(
    var productId: Int,
    var description: String,
    var price_k: Double,
    var price_s: Double
)

data class ResUploader(
    var url: String?,
    var success: Boolean
)

data class RequestBodyX(
    var key: String,
    var method: String,
    var parameters: Map<String, *>
)

data class DataMerchant(
    var brands: List<String>,
    var materials: List<Material>,
    var products: List<SurveyProduct>
)

data class BrandSale (
    var brand: String,
    var listMeasure: List<String>
)

data class Question (
    var score: String,
    var text: String,
    var flag: Boolean
)

data class CheckSupPromoter (
    var key: String,
    var decription: String,
    var type: String,
    var flag: Boolean
)


data class DataPromoter(
    var merchandises: List<Merchandise>,
    var saleBrand: List<BrandSale>,
    var stocks: List<Stock>,
    var stocksSelected: List<Stock>,
    var productsSelected: List<SurveyProduct>,
)


data class DataSupervisor(
    var markets: List<Market>,
    var questions: List<Question>,
    var checks: List<CheckSupPromoter>,
)

data class Seltmp(
    var replaceStock: List<Stock>,
    var Sale: List<SurveyProduct>,
)