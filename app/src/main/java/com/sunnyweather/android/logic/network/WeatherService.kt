package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.CollectRealtimeResponse
import com.sunnyweather.android.logic.model.DailyResponse
import com.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherService {

    /*
    * https://api.caiyunapp.com/v2.5/{token}/121.6544,25.1552/realtime.json
    * Path占位符,调用方法发起请求时，Retrofit会自动将参数值替换到占位符的位置
    * */
    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<RealtimeResponse>

    /*
    * https://api.caiyunapp.com/v2.5/{token}/121.6544,25.1552/realtime.json
    * Path占位符,调用方法发起请求时，Retrofit会自动将参数值替换到占位符的位置
    * */
    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
    fun getRealtimeWeatherCollect(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<CollectRealtimeResponse>


    /*
    * https://api.caiyunapp.com/v2.5/{token}/121.6544,25.1552/daily.json
    * Path占位符,调用方法发起请求时，Retrofit会自动将参数值替换到占位符的位置
    * */
    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/daily.json")
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<DailyResponse>

}