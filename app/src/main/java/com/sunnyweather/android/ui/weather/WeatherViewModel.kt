package com.sunnyweather.android.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location

class WeatherViewModel : ViewModel() {

    private val locationLiveData = MutableLiveData<Location>()

    var loactionLng = ""

    var locationLat = ""

    var placeName = ""

    var cityName = ""

    var districtName = ""

    var tag = ""  //根据tag0/1控制是否开启网络请求获取城市信息 1不获取 0获取

    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->
        Repository.refreshWeather(location.lng, location.lat)
    }

    fun refreshWeather(lng: String, lat: String){
        locationLiveData.value = Location(lng, lat) //将传入的经纬度封装成一个Location对象
    }

    /*
    * 在ViewModel中进行多一次封装，CityDao
    * */
    fun saveCity(city: String, district: String) = Repository.saveCity(city, district)

    fun getSavedCity() = Repository.getSavedCity()

    fun isCitySaved() = Repository.isCitySaved()

    fun clearCitySaved() = Repository.clearCitySaved()

}