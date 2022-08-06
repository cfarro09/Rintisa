package com.delycomps.rintisa.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Visit2(
    @SerializedName("status")
    @Expose
    var status: String,
    @SerializedName("visit_finish")
    @Expose
    var visitFinish: String,
    @SerializedName("visit_start")
    @Expose
    var visitStart: String,
    @SerializedName("customerid")
    @Expose
    var customerId: Int,
    @SerializedName("customer")
    @Expose
    var customer: String,
    @SerializedName("address")
    @Expose
    var address: String,
    @SerializedName("semaphore")
    @Expose
    var semaphore: String,
    @SerializedName("code")
    @Expose
    var code: String,
    @SerializedName("market")
    @Expose
    var market: String,
)
