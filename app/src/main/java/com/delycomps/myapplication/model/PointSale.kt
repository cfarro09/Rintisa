package com.delycomps.myapplication.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

open class PointSale: Parcelable {
    @SerializedName("visitid")
    @Expose
    var visitId: Int
    @SerializedName("customerid")
    @Expose
    var customerId: Int
    @SerializedName("code")
    @Expose
    var clientCode: String?
    @SerializedName("customer_name")
    @Expose
    var client: String?
    @SerializedName("market_name")
    @Expose
    var market: String?
    @SerializedName("stand")
    @Expose
    var stallNumber: String? = ""
    @SerializedName("frecuency")
    @Expose
    var visitFrequency: String? = ""
    @SerializedName("visit_date")
    @Expose
    var visitDay: String? = ""
    @SerializedName("last_visit")
    @Expose
    var lastVisit: String? = ""
    @SerializedName("semaphore")
    @Expose
    var trafficLights: String? = ""
    @SerializedName("showsurvey")
    @Expose
    var showSurvey: Boolean = false
    @SerializedName("showavailability")
    @Expose
    var showAvailability: Boolean = false
    @SerializedName("management")
    @Expose
    var management: String? = ""
    @SerializedName("image_before")
    @Expose
    var imageBefore: String? = ""
    @SerializedName("image_after")
    @Expose
    var imageAfter: String? = ""
    @SerializedName("uuid")
    @Expose
    var uuid: String? = ""

    constructor(
        visitId: Int,
        customerId: Int,
        clientCode: String,
        client: String,
        market: String,
        stallNumber: String,
        visitFrequency: String,
        visitDay: String,
        lastVisit: String,
        trafficLights: String,
        showSurvey: Boolean,
        showAvailability: Boolean,
        management: String,
        imageBefore: String,
        imageAfter: String,
        uuid: String? = UUID.randomUUID().toString()
    ) {
        this.visitId = visitId
        this.customerId = customerId
        this.clientCode = clientCode
        this.client = client
        this.market = market
        this.stallNumber = stallNumber
        this.visitFrequency = visitFrequency
        this.visitDay = visitDay
        this.lastVisit = lastVisit
        this.trafficLights = trafficLights
        this.showSurvey = showSurvey
        this.showAvailability = showAvailability
        this.management = management
        this.imageBefore = imageBefore
        this.imageAfter = imageAfter
        this.uuid = uuid
    }
    protected constructor(parcel: Parcel) {
        this.visitId = parcel.readInt()
        this.customerId = parcel.readInt()
        this.clientCode = parcel.readString()
        this.client = parcel.readString()
        this.market = parcel.readString()
        this.stallNumber = parcel.readString()
        this.visitFrequency = parcel.readString()
        this.visitDay = parcel.readString()
        this.lastVisit = parcel.readString()
        this.trafficLights = parcel.readString()
        this.showSurvey = parcel.readInt() != 0
        this.showAvailability = parcel.readInt() != 0
        this.management = parcel.readString()
        this.imageBefore = parcel.readString()
        this.imageAfter = parcel.readString()
        this.uuid = parcel.readString()
    }
    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(visitId)
        p0.writeInt(customerId)
        p0.writeString(clientCode)
        p0.writeString(client)
        p0.writeString(market)
        p0.writeString(stallNumber)
        p0.writeString(visitFrequency)
        p0.writeString(visitDay)
        p0.writeString(lastVisit)
        p0.writeString(trafficLights)
        p0.writeByte((if (showSurvey) 1 else 0).toByte())
        p0.writeByte((if (showAvailability) 1 else 0).toByte())
        p0.writeString(management)
        p0.writeString(imageBefore)
        p0.writeString(imageAfter)
        p0.writeString(uuid)
    }
    companion object CREATOR : Parcelable.Creator<PointSale> {
        override fun createFromParcel(parcel: Parcel): PointSale {
            return PointSale(parcel)
        }

        override fun newArray(size: Int): Array<PointSale?> {
            return arrayOfNulls(size)
        }
    }
}