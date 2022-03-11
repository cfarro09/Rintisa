package com.delycomps.myapplication.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Material: Parcelable {
    @SerializedName("material")
    @Expose
    var material: String?
    @SerializedName("brand")
    @Expose
    var brand: String?
    @SerializedName("quantity")
    @Expose
    var quantity: Int

    constructor(
        material: String,
        brand: String,
        quantity: Int,

    ) {
        this.material = material
        this.brand = brand
        this.quantity = quantity

    }
    protected constructor(parcel: Parcel) {
        this.material = parcel.readString()
        this.brand = parcel.readString()
        this.quantity = parcel.readInt()

    }
    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(quantity)
        p0.writeString(material)
        p0.writeString(brand)

    }
    companion object CREATOR : Parcelable.Creator<Material> {
        override fun createFromParcel(parcel: Parcel): Material {
            return Material(parcel)
        }

        override fun newArray(size: Int): Array<Material?> {
            return arrayOfNulls(size)
        }
    }
}