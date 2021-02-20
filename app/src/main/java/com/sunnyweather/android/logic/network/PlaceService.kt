package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.PlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PlaceService {

    //http://api.caiyunapp.com/v2/place?Query=北京&token={token}&lang=zh_CN
    //API中的query参数需要动态指定，所以用@Query注解的方式来实现，另外两个参数是不会变的，固定写在@GET注解中
    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")
    fun searchPlaces(@Query("query") query: String) : Call<PlaceResponse>

}