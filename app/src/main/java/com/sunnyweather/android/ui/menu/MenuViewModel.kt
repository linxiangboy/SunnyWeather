package com.sunnyweather.android.ui.menu

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.LngLatCityDisSkyTem
import com.sunnyweather.android.logic.model.MenuCollect


class MenuViewModel : ViewModel() {

    //用于对界面上显示的数据进行缓存
    val menulist = ArrayList<MenuCollect>()


    private val locationlistLiveData = MutableLiveData<List<String>>()

    val weatherListLiveData = Transformations.switchMap(locationlistLiveData) { location ->
        Repository.refreshWeatherList(location)
    }

    fun refreshWeatherList(location: List<String>){
        locationlistLiveData.value = location //将传入的经纬度封装成一个Location对象
    }

    fun CreateMenuSqlDao(context: Context, name: String, version: Int) = Repository.CreateMenuSqlDao(context, name, version)

}