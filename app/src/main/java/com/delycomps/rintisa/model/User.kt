package com.delycomps.rintisa.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("token")
    @Expose
    var token: String,
    @SerializedName("firstname")
    @Expose
    var firstName: String,
    @SerializedName("lastname")
    @Expose
    var lastName: String,
    @SerializedName("fullname")
    @Expose
    var fullname: String,
    @SerializedName("usr")
    @Expose
    var username: String,
    @SerializedName("roledesc")
    @Expose
    var role: String,
    @SerializedName("email")
    @Expose
    var email: String,
    @SerializedName("userid")
    @Expose
    var userId: Int,
)
