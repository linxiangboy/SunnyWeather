package com.sunnyweather.android.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.dao.PlaceDao

class HomeViewModel : ViewModel() {

    private val iplnglatLiveData = MutableLiveData<String>()

    val queryIpLngLatLiveData = Transformations.switchMap(iplnglatLiveData){
        Repository.getQueryIp_LngLat()
    }

    fun getQueryIp_LngLat( ){
        iplnglatLiveData.value = "1"
    }

    //在本地存储的lnglatcity数据
    fun saveLngLatCity(lnglatCity: String) = Repository.saveLngLatCity(lnglatCity)
    fun getSavedLngLatCity() = Repository.getSavedLngLatCity()
    fun isLngLatCitySaved() = Repository.isLngLatCitySaved()

}