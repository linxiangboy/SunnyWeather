package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.DailyResponse
import com.sunnyweather.android.logic.model.DistrictResponse
import com.sunnyweather.android.logic.model.Location
import com.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherService {

    /*
    * https://api.caiyunapp.com/v2.5/{token}/121.6544,25.1552/realtime.json
    * Path占位符,调用方法发起请求时，Retrofit会自动将参数值替换到占位符的位置
    * */
    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<RealtimeResponse>


    /*
    * https://api.caiyunapp.com/v2.5/{token}/121.6544,25.1552/daily.json
    * Path占位符,调用方法发起请求时，Retrofit会自动将参数值替换到占位符的位置
    * */
    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/daily.json")
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<DailyResponse>


    /*
    * https://restapi.amap.com/v3/geocode/regeo?key=1d18680afacd7c5380e1c6fca867805b&location=114.246899,22.720968
    * &poitype=&radius=&extensions=base&batch=false&roadlevel=
    * */
    @GET("v3/geocode/regeo?key=${SunnyWeatherApplication.GeoKey}&poitype=&radius=&extensions=base&batch=false&roadlevel=")
    fun getDistrict(@Query("location") location: String):
            Call<DistrictResponse>

}