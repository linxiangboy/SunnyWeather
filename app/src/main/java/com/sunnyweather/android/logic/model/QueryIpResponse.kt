package com.sunnyweather.android.logic.model
import com.google.gson.annotations.SerializedName


data class QueryIpResponse(
    @SerializedName("adcode")
    val adcode: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("info")
    val info: String,
    @SerializedName("province")
    val province: String,
    @SerializedName("status")
    val status: String
)