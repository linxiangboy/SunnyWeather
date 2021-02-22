package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.Tool.LogUtil
import com.sunnyweather.android.logic.model.Place

object CityDao {

    /*
 * savePlace()方法用于将Place对象存储到SharedPreferences对象中
 * 通过Gson().toJson()方法将Place对象转换成一个json数据
 * */
    fun saveCity(city: String, district: String){
        sharedPreferences().edit {
            putString("cityD", "$city&$district")
        }
    }

    /*
    * 先将json字符串从SharedPreferences中取出来
    * 再通过Gson().fromJson()方法将json字符串解析成Place类型的数据
    * */
    fun getSavedCity(): String {
        val cityD = sharedPreferences().getString("cityD", "") // deValue默认值
        return cityD.toString()
    }

    fun isCitySaved() = sharedPreferences().contains("cityD") //用于判断是否有数据已被存储

    //清除city_district内的数据
    fun clearCitySaved(){
        LogUtil.d("yyy","清除成功")
        sharedPreferences().edit().clear()
    }

    private fun sharedPreferences() = SunnyWeatherApplication.context
        .getSharedPreferences("city_district", Context.MODE_PRIVATE)

}