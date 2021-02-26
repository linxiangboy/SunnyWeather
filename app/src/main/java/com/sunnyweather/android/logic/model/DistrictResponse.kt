package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName


data class DistrictResponse(
    @SerializedName("info")
    val info: String,
    @SerializedName("regeocode")
    val regeocode: Regeocode,
    @SerializedName("status")
    val status: String
)

data class Regeocode(
    @SerializedName("addressComponent")
    val addressComponent: AddressComponent
)

data class AddressComponent(
    @SerializedName("city")
    val city: String,
    @SerializedName("district")
    val district: String,
    @SerializedName("province")
    val province: String
)