package com.sunnyweather.android.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository

class WeatherViewModel : ViewModel() {
    var location = ""

    var cityName = ""

    var districtName = ""

    private val locationLiveData = MutableLiveData<String>()

    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->
        Repository.refreshWeather(location)
    }

    fun refreshWeather(location: String){
        locationLiveData.value = location //将传入的经纬度封装成一个Location对象
    }


    //传入地区名获取天气信息
    private val lnglatLiveData = MutableLiveData<String>()

    val queryLngLatLiveData = Transformations.switchMap(lnglatLiveData){ address ->
        Repository.getQuery_LngLat(address)
    }

    fun getQueryLngLat(address: String){
        lnglatLiveData.value = address
    }


    fun saveLngLatCity(lnglatCity: String) = Repository.saveLngLatCity(lnglatCity)


}