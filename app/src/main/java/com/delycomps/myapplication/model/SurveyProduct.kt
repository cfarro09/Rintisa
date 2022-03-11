package com.delycomps.myapplication.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class SurveyProduct: Parcelable {
    @SerializedName("productid")
    @Expose
    var productId: Int
    @SerializedName("description")
    @Expose
    var description: String?
    @SerializedName("price")
    @Expose
    var price: Double
    @SerializedName("measure_unit")
    @Expose
    var measureUnit: String?
    @SerializedName("brand")
    @Expose
    var brand: String?
    @SerializedName("quantity")
    @Expose
    var quantity: Int
    @SerializedName("competence")
    @Expose
    var competence: String?

    constructor(
        productId: Int,
        description: String,
        brand: String,
        price: Double,
        measureUnit: String,
        quantity: Int = 0,
        competence: String? = ""
    ) {
        this.productId = productId
        this.description = description
        this.brand = brand
        this.price = price
        this.measureUnit = measureUnit
        this.quantity = quantity
        this.competence = competence
    }
    protected constructor(parcel: Parcel) {
        this.productId = parcel.readInt()
        this.description = parcel.readString()
        this.brand = parcel.readString()
        this.price = parcel.readDouble()
        this.measureUnit = parcel.readString()
        this.quantity = parcel.readInt()
        this.competence = parcel.readString()
    }
    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(productId)
        p0.writeString(description)
        p0.writeString(brand)
        p0.writeDouble(price)
        p0.writeString(measureUnit)
        p0.writeInt(quantity)
        p0.writeString(competence)
    }
    companion object CREATOR : Parcelable.Creator<SurveyProduct> {
        override fun createFromParcel(parcel: Parcel): SurveyProduct {
            return SurveyProduct(parcel)
        }

        override fun newArray(size: Int): Array<SurveyProduct?> {
            return arrayOfNulls(size)
        }
    }
}