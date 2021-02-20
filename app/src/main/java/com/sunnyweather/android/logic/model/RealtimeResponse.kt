package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName


//将所有的数据模型类定义在RealtimeResponse内部可以防止出现与其他接口的数据模型类同名冲突的情况
data class RealtimeResponse(val status: String, val result: Result){

    data class Result(val realtime: Realtime)

    //数据
    /*
    * temperature温度
    * skycon天气情况
    * airQuality空气质量
    * */
    data class Realtime(val temperature: Float, val skycon: String,
                        @SerializedName("air_quality") val airQuality: AirQuality)

    data class AirQuality(val aqi: AQI)

    data class AQI(val chn: Float)

}