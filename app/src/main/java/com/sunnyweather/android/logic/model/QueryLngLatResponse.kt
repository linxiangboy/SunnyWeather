package com.sunnyweather.android.logic.model
import com.google.gson.annotations.SerializedName


data class QueryLngLatResponse(
    @SerializedName("geocodes")
    val geocodes: List<Geocodes>,
    @SerializedName("info")
    val info: String,
    @SerializedName("status")
    val status: String
)

data class Geocodes(
    @SerializedName("city")
    val city: String,
    @SerializedName("location")
    val location: String
)