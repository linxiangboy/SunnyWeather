package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

data class CollectResponseshare(val lat: String, val lng: String, val city: String, val district: String) //存储到本地和从本地拿出来的数据

data class CollectResponse(val lat: String, val lng: String, val city: String, val district: String, val temperature: String, val skycon: String) //显示在PlaceFragment上的数据

data class CollectRealtimeResponse(val status: String, val result: Result, val location: List<String>){ //网络请求的数据

    data class Result(val realtime: Realtime)
    //数据
    /*
    * temperature温度
    * skycon天气情况
    * */
    data class Realtime(val temperature: Float, val skycon: String)

}