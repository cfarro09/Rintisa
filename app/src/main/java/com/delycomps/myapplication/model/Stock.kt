package com.delycomps.myapplication.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Stock: Parcelable {
    @SerializedName("type")
    @Expose
    var type: String?
    @SerializedName("brand")
    @Expose
    var brand: String?
    @SerializedName("flag")
    @Expose
    var flag: Boolean? = false
    @SerializedName("product")
    @Expose
    var product: String?

    constructor(
        type: String,
        brand: String,
        product: String,
        flag: Boolean? = false
    ) {
        this.type = type
        this.brand = brand
        this.product = product
        this.flag = flag

    }
    protected constructor(parcel: Parcel) {
        this.type = parcel.readString()
        this.brand = parcel.readString()
        this.product = parcel.readString()
        this.flag = parcel.readInt() != 0
    }
    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(type)
        p0.writeString(brand)
        p0.writeString(product)
        p0.writeByte((if (flag == true) 1 else 0).toByte())

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