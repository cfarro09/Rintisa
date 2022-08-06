package com.delycomps.rintisa.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserSup(
    @SerializedName("hour_entry")
    @Expose
    var hourEntry: String,
    @SerializedName("hour_init_break")
    @Expose
    var hourInitBreak: String,
    @SerializedName("hour_finish_break")
    @Expose
    var hourFinishBreak: String,
    @SerializedName("hour_exit")
    @Expose
    var hourExit: String,
    @SerializedName("usr")
    @Expose
    var User: String,
    @SerializedName("userid")
    @Expose
    var userId: Int,
    @SerializedName("finish_visit")
    @Expose
    var finishVisit: Int,
    @SerializedName("finish_success_visit")
    @Expose
    var finishSuccessVisit: Int,
    @SerializedName("initiated_visit")
    @Expose
    var initiatedVisit: Int,
    @SerializedName("without_visit")
    @Expose
    var withoutVisit: Int,
)
