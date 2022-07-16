package com.delycomps.rintisa.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

open class Stock: Parcelable {
    @SerializedName("type")
    @Expose
    var type: String?
    @SerializedName("brand")
    @Expose
    var brand: String?
    @SerializedName("product")
    @Expose
    var product: String?
    var uuid: String?
    var flag: Boolean

    constructor(
        type: String,
        brand: String,
        product: String,
        uuid: String =  UUID.randomUUID().toString(),
        flag: Boolean = false
    ) {
        this.type = type
        this.brand = brand
        this.product = product
        this.uuid = uuid
        this.flag = flag
    }
    protected constructor(parcel: Parcel) {
        this.type = parcel.readString()
        this.brand = parcel.readString()
        this.product = parcel.readString()
        this.uuid = parcel.readString()
        this.flag = parcel.readInt() != 0

    }
    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(type)
        p0.writeString(brand)
        p0.writeString(product)
        p0.writeString(uuid)
        p0.writeByte((if (flag) 1 else 0).toByte())

    }
    companion object CREATOR : Parcelable.Creator<Stock> {
        override fun createFromParcel(parcel: Parcel): Stock {
            return Stock(parcel)
        }

        override fun newArray(size: Int): Array<Stock?> {
            return arrayOfNulls(size)
        }
    }
}