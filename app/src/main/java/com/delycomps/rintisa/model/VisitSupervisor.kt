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


    @SerializedName("speechSCN")
    @Expose
    var speechSCN: String? = ""
    @SerializedName("speechRCN")
    @Expose
    var speechRCN: String? = ""
    @SerializedName("speechRCT")
    @Expose
    var speechRCT: String? = ""
    @SerializedName("speechSCT")
    @Expose
    var speechSCT: String? = ""
    @SerializedName("uniformJson")
    @Expose
    var uniformJson: String? = ""
    @SerializedName("materialJson")
    @Expose
    var materialJson: String? = ""
    @SerializedName("statusJson")
    @Expose
    var statusJson: String? = ""
    @SerializedName("useridselected")
    @Expose
    var userIdSelected: Int

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
        speechSCN: String? = "",
        speechRCN: String? = "",
        speechRCT: String? = "",
        speechSCT: String? = "",
        uniformJson: String? = "",
        materialJson: String? = "",
        statusJson: String? = "",
        userIdSelected: Int = 0,
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
        this.speechSCN = speechSCN
        this.speechRCN = speechRCN
        this.speechRCT = speechRCT
        this.speechSCT = speechSCT
        this.uniformJson = uniformJson
        this.materialJson = materialJson
        this.statusJson = statusJson
        this.userIdSelected = userIdSelected
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

        this.speechSCN = parcel.readString()
        this.speechRCN = parcel.readString()
        this.speechRCT = parcel.readString()
        this.speechSCT = parcel.readString()
        this.uniformJson = parcel.readString()
        this.materialJson = parcel.readString()
        this.statusJson = parcel.readString()
        this.userIdSelected = parcel.readInt()

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
        p0.writeString(speechSCN)
        p0.writeString(speechRCN)
        p0.writeString(speechRCT)
        p0.writeString(speechSCT)
        p0.writeString(uniformJson)
        p0.writeString(materialJson)
        p0.writeString(statusJson)
        p0.writeInt(userIdSelected)

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