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

data class ResponseMulti(
    var success: Boolean?,
    var data: List<ResponseList<Map<String, Any>>>,
)

data class ResError(
    var msg: String?,
    var code: Int?
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

data class DataPromoter(
    var merchandises: List<Merchandise>,
    var products: List<SurveyProduct>,
    var stocks: List<Stock>,
    var stocksSelected: List<Stock>,
    var productsSelected: List<SurveyProduct>,
)

data class Seltmp(
    var replaceStock: List<Stock>,
    var Sale: List<SurveyProduct>,
)