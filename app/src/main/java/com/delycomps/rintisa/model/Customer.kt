package com.delycomps.rintisa.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

open class Customer: Parcelable {

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
    @SerializedName("semaphore")
    @Expose
    var trafficLights: String? = ""
    @SerializedName("uuid")
    @Expose
    var uuid: String? = ""
    @SerializedName("comment")
    @Expose
    var comment: String? = ""
    @SerializedName("status")
    @Expose
    var status: String? = ""

    constructor(
        customerId: Int,
        clientCode: String,
        client: String,
        market: String,
        stallNumber: String,
        visitFrequency: String,
        trafficLights: String,
        uuid: String? = UUID.randomUUID().toString(),
        comment: String? = "",
        customer: String? = "",
    ) {
        this.customerId = customerId
        this.clientCode = clientCode
        this.client = client
        this.market = market
        this.stallNumber = stallNumber
        this.visitFrequency = visitFrequency
        this.trafficLights = trafficLights
        this.uuid = uuid
        this.comment = comment
        this.status = status
    }
    protected constructor(parcel: Parcel) {
        this.customerId = parcel.readInt()
        this.clientCode = parcel.readString()
        this.client = parcel.readString()
        this.market = parcel.readString()
        this.stallNumber = parcel.readString()
        this.visitFrequency = parcel.readString()
        this.trafficLights = parcel.readString()
        this.uuid = parcel.readString()
        this.comment = parcel.readString()
        this.status = parcel.readString()
    }
    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(customerId)
        p0.writeString(clientCode)
        p0.writeString(client)
        p0.writeString(market)
        p0.writeString(stallNumber)
        p0.writeString(visitFrequency)
        p0.writeString(trafficLights)
        p0.writeString(uuid)
        p0.writeString(comment)
        p0.writeString(status)
    }
    companion object CREATOR : Parcelable.Creator<Customer> {
        override fun createFromParcel(parcel: Parcel): Customer {
            return Customer(parcel)
        }

        override fun newArray(size: Int): Array<Customer?> {
            return arrayOfNulls(size)
        }
    }
}