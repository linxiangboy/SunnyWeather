package com.sunnyweather.android.logic.network

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetwork {

    private val placeService = ServiceCreator.create<PlaceService>() //创建PlaceService接口的动态代理对象

    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query)
        .await() //定义一个searchPlaces()函数，在这里调用PlaceService接口中定义的searchPlaces()方法，以发起搜索城市数据请求

    //当外部调用searchPlaces()函数时，Retrofit会立即发起网络请求，并阻塞当前协程，直到服务器响应我们的请求之后，await()函数会将解析出来的数据模型对象取出并返回
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
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

}