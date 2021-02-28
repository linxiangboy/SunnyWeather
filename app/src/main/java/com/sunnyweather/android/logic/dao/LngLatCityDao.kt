package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.sunnyweather.android.SunnyWeatherApplication

object LngLatCityDao {

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