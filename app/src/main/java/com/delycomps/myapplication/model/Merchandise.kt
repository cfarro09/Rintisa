package com.delycomps.myapplication.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Merchandise(
    var description: String,
    var flag: Boolean? = false,
)
