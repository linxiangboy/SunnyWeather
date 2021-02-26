package com.sunnyweather.android.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceCreator {

    //const 只读且不能初始化
    private const val BASE_URL = "https://api.caiyunapp.com/" //彩云天气根地址

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> create(serviceCall: Class<T>): T = retrofit.create(serviceCall)

    //不带参数的create
    inline fun <reified T> create(): T = create(T::class.java)



    private const val BASE_URL_Geo = "https://restapi.amap.com/" //高德地图根地址

    private val retrofit1 = Retrofit.Builder()
        .baseUrl(BASE_URL_Geo)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> createGeo(serviceCall: Class<T>): T = retrofit1.create(serviceCall)

    inline fun <reified T> createGeo(): T = createGeo(T::class.java)

}