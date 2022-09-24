package com.delycomps.rintisa.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

open class UserFromAuditor: Parcelable {
    @SerializedName("description")
    @Expose
    var description: String? = ""
    @SerializedName("docnum")
    @Expose
    var docnum: String? = ""
    @SerializedName("phone")
    @Expose
    var phone: String? = ""
    @SerializedName("sector")
    @Expose
    var sector: String? = ""
    @SerializedName("userid")
    @Expose
    var userid: Int
    @SerializedName("status")
    @Expose
    var status: String? = ""

    constructor(
        description: String,
        docnum: String,
        phone: String,
        sector: String,
        userid: Int,
        status: String
    ) {
        this.description = description
        this.docnum = docnum
        this.phone = phone
        this.sector = sector
        this.userid = userid
        this.status = status
    }
    protected constructor(parcel: Parcel) {
        this.description = parcel.readString()
        this.docnum = parcel.readString()
        this.phone = parcel.readString()
        this.sector = parcel.readString()
        this.userid = parcel.readInt()
        this.status = parcel.readString()
    }
    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(description)
        p0.writeString(docnum)
        p0.writeString(phone)
        p0.writeString(sector)
        p0.writeInt(userid)
        p0.writeString(status)
    }
    companion object CREATOR : Parcelable.Creator<UserFromAuditor> {
        override fun createFromParcel(parcel: Parcel): UserFromAuditor {
            return UserFromAuditor(parcel)
        }

        override fun newArray(size: Int): Array<UserFromAuditor?> {
            return arrayOfNulls(size)
        }
    }

}
