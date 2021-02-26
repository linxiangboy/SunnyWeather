package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

object PlaceDao {

    /*
    * savePlace()方法用于将Place对象存储到SharedPreferences对象中
    * 通过Gson().toJson()方法将Place对象转换成一个json数据
    * */
    fun savePlace(place: Place){
        sharedPreferences().edit {
            putString("place", Gson().toJson(place))
        }
    }

    /*
    * 先将json字符串从SharedPreferences中取出来
    * 再通过Gson().fromJson()方法将json字符串解析成Place类型的数据
    * */
    fun getSavedPlace(): Place{
        val placeJson = sharedPreferences().getString("place", "") // deValue默认值
        return Gson().fromJson(placeJson, Place::class.java)
    }

    fun isPlaceSaved() = sharedPreferences().contains("place") //用于判断是否有数据已被存储

    private fun sharedPreferences() = SunnyWeatherApplication.context
        .getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)




    //存储在本地的lnglatcity数据
    fun saveLngLatCity(lnglatcity: String){
        sharedPreferenceslnglatcity().edit {
            putString("lnglat_city", lnglatcity)
        }
    }

    fun getSavedLngLatCity(): String? {
        return sharedPreferenceslnglatcity().getString("lnglat_city", "")
    }

    fun isLngLatCitySaved() = sharedPreferenceslnglatcity().contains("lnglat_city")

    private fun sharedPreferenceslnglatcity() = SunnyWeatherApplication.context
        .getSharedPreferences("home_dao", Context.MODE_PRIVATE)


}