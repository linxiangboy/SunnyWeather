package com.sunnyweather.android.ui.weather

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository

class WeatherViewModel : ViewModel() {
    var location = ""

    var cityName = ""

    var districtName = ""

    var realtime = ""

    var daily = ""

    var collectTag = 0 //0 = 未收藏; 1 = 收藏

    var homecity = "false" //是否为主页城市 false不是 true是


    //传入lnglat获取天气数据
    private val locationLiveData = MutableLiveData<String>()

    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->
        Repository.refreshWeather(location)
    }

    fun refreshWeather(location: String){
        locationLiveData.value = location //将传入的经纬度封装成一个Location对象
    }


    //传入地区名获取lnglat数据
    private val lnglatLiveData = MutableLiveData<String>()

    val queryLngLatLiveData = Transformations.switchMap(lnglatLiveData){ address ->
        Repository.getQuery_LngLat(address)
    }

    fun getQueryLngLat(address: String){
        lnglatLiveData.value = address
    }


//    fun saveLngLatCity(lnglatCity: String) = Repository.saveLngLatCity(lnglatCity)


    fun CreateMenuSqlDao(context: Context, name: String, version: Int) = Repository.CreateMenuSqlDao(context, name, version)


}