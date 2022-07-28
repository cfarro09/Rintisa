package com.delycomps.rintisa.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

open class VisitSupervisor: Parcelable {
    @SerializedName("customer_id")
    @Expose
    var customerId: Int
    @SerializedName("userid")
    @Expose
    var userId: Int
    @SerializedName("userid_created")
    @Expose
    var userIdCreated: Int
    @SerializedName("date")
    @Expose
    var date: String? = ""
    @SerializedName("create_date")
    @Expose
    var createDate: String? = ""
    @SerializedName("image1")
    @Expose
    var image1: String? = ""
    @SerializedName("type")
    @Expose
    var type: String? = ""
    @SerializedName("latitude")
    @Expose
    var latitude: Double
    @SerializedName("longitude")
    @Expose
    var longitude: Double

    @SerializedName("comment")
    @Expose
    var comment: String? = ""
    @SerializedName("auditjson")
    @Expose
    var auditjson: String? = ""
    @SerializedName("status")
    @Expose
    var status: String? = ""
    @SerializedName("customer")
    @Expose
    var customer: String? = ""
    @SerializedName("visitid")
    @Expose
    var visitId: Int
    @SerializedName("image2")
    @Expose
    var image2: String? = ""
    @SerializedName("image3")
    @Expose
    var image3: String? = ""
    @SerializedName("image4")
    @Expose
    var image4: String? = ""
    @SerializedName("image5")
    @Expose
    var image5: String? = ""

    var uuid: String?

    constructor(
        customerId: Int = 0,
        userId: Int = 0,
        userIdCreated: Int = 0,
        date: String = "",
        createDate: String = "",
        image1: String = "",
        type: String = "",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        comment: String? = "",
        auditjson: String? = "",
        status: String? = "",
        customer: String? = "",
        visitId: Int = 0,
        image2: String? = "",
        image3: String? = "",
        image4: String? = "",
        image5: String? = "",
        uuid: String? =  UUID.randomUUID().toString()
    ) {
        this.customerId = customerId
        this.userId = userId
        this.userIdCreated = userIdCreated
        this.date = date
        this.createDate = createDate
        this.image1 = image1
        this.type = type
        this.comment = comment
        this.auditjson = auditjson
        this.latitude = latitude
        this.longitude = longitude
        this.comment = comment
        this.auditjson = auditjson
        this.status = status
        this.customer = customer
        this.visitId = visitId
        this.image2 = image2
        this.image3 = image3
        this.image4 = image4
        this.image5 = image5
        this.uuid = uuid
    }

    protected constructor(parcel: Parcel) {
        this.customerId = parcel.readInt()
        this.userId = parcel.readInt()
        this.userIdCreated = parcel.readInt()
        this.date = parcel.readString()
        this.createDate = parcel.readString()
        this.image1 = parcel.readString()
        this.type = parcel.readString()
        this.latitude = parcel.readDouble()
        this.longitude = parcel.readDouble()
        this.auditjson = parcel.readString()
        this.comment = parcel.readString()
        this.status = parcel.readString()
        this.customer = parcel.readString()
        this.visitId = parcel.readInt()
        this.image2 = parcel.readString()
        this.image3 = parcel.readString()
        this.image4 = parcel.readString()
        this.image5 = parcel.readString()
        this.uuid = parcel.readString()
    }
    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(customerId)
        p0.writeInt(userId)
        p0.writeInt(userIdCreated)
        p0.writeString(date)
        p0.writeString(createDate)
        p0.writeString(image1)
        p0.writeString(type)
        p0.writeDouble(latitude)
        p0.writeDouble(longitude)
        p0.writeString(comment)
        p0.writeString(auditjson)
        p0.writeString(status)
        p0.writeString(customer)
        p0.writeInt(visitId)
        p0.writeString(image2)
        p0.writeString(image3)
        p0.writeString(image4)
        p0.writeString(image5)
        p0.writeString(uuid)

    }
    companion object CREATOR : Parcelable.Creator<VisitSupervisor> {
        override fun createFromParcel(parcel: Parcel): VisitSupervisor {
            return VisitSupervisor(parcel)
        }

        override fun newArray(size: Int): Array<VisitSupervisor?> {
            return arrayOfNulls(size)
        }
    }
}