package com.delycomps.rintisa.api

import com.delycomps.rintisa.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Connection {

    val instance: Endpoints = Retrofit.Builder().run {
        baseUrl(Constants.HOST)
        addConverterFactory(GsonConverterFactory.create())
        build()
    }.create(Endpoints::class.java)
}
