package com.delycomps.rintisa.model

data class Merchandise(
    var description: String,
    var flag: Boolean? = false,
    var brand: String? = "",
    var merchandisingid: Int? = 0,
    var quantity: Int? = 0,
)
