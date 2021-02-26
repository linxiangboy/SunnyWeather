package com.sunnyweather.android.logic.network

import com.sunnyweather.android.logic.model.QueryLngLatResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//网络数据源访问入口
object SunnyWeatherNetwork {
    //搜索城市
    private val placeService = ServiceCreator.create<PlaceService>() //创建PlaceService接口的动态代理对象

    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query)
        .await() //定义一个searchPlaces()函数，在这里调用PlaceService接口中定义的searchPlaces()方法，以发起搜索城市数据请求


    //获取天气状况
    private val weatherService = ServiceCreator.create<WeatherService>()

    suspend fun getDailyWeather(location: String) =
        weatherService.getDailyWeather(location).await()

    suspend fun getRealtimeWeather(location: String) =
        weatherService.getRealtimeWeather(location).await()

    //lng,lat获取地区名称
    private val districtService = ServiceCreator.createGeo<WeatherService>()

    suspend fun getDistrict(location: String) =
        districtService.getDistrict(location).await()


    //获取IP地址对应的城市
    private val queryIpService = ServiceCreator.createGeo<QueryIpLatlngService>()

    suspend fun getQueryIp() = queryIpService.getQueryIp().await()


    //用城市名获取经纬度和city
    private val queryLngLatService = ServiceCreator.createGeo<QueryIpLatlngService>()

    suspend fun getQueryLngLat(address: String) = queryLngLatService.getQueryLngLat(address).await()

}



    //当外部调用searchPlaces()函数时，Retrofit会立即发起网络请求，并阻塞当前协程，直到服务器响应请求之后，await()函数会将解析出来的数据模型对象取出并返回
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null){
                        continuation.resume(body)
                    }
                    else continuation.resumeWithException(
                        RuntimeException("response body is null")
                    )
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

