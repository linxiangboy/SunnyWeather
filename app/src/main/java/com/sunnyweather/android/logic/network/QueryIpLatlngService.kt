package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.QueryIpResponse
import com.sunnyweather.android.logic.model.QueryLngLatResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QueryIpLatlngService {

    //https://restapi.amap.com/v3/ip?key=<用户的key>
    @GET("v3/ip?key=${SunnyWeatherApplication.GeoKey}")
    fun getQueryIp() : Call<QueryIpResponse>

    //restapi.amap.com/v3/geocode/geo?key=您的key&address=广东省深圳市
    @GET("v3/geocode/geo?key=${SunnyWeatherApplication.GeoKey}")
    fun getQueryLngLat(@Query("address") address: String) : Call<QueryLngLatResponse>

}