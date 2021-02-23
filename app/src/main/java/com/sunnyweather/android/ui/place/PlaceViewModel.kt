package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.CollectRealtimeResponse
import com.sunnyweather.android.logic.model.CollectResponse
import com.sunnyweather.android.logic.model.Location
import com.sunnyweather.android.logic.model.Place

class PlaceViewModel  : ViewModel() {

    /*
    * 将传入的搜索值参数复制给一个searchLiveData对象
    * 并使用Transformations的switchMap()方法来观察这个对象
    *
    * 每当searchPlaces()函数被调用时，switchMap()方法所对应的转换函数就会执行
    * 在转换函数中，调用仓库层中定义的searchPlaces()方法发起网络请求
    * 同时将仓库层返回的LiveData对象转换成一个可供Activity观察的LiveData对象
    * */

    private val searchLiveData = MutableLiveData<String>()

    //用于对界面上显示的数据进行缓存
    val placeList = ArrayList<Place>()

    val placeLiveData = Transformations.switchMap(searchLiveData){ query ->
        Repository.searchPlaces(query)
    }

    fun searchPlaces(query: String){
        searchLiveData.value = query
    }


    private val collectLocationLiveData = MutableLiveData<Location>()
    val collectList = ArrayList<CollectResponse>() //用于对界面上显示的数据进行缓存
    val collectLiveData = Transformations.switchMap(collectLocationLiveData){ location ->
        Repository.refreshCollect(location.lng, location.lat)
    }
    fun refreshCollect(lng: String, lat: String){
        collectLocationLiveData.value = Location(lng, lat)
    }

    /*
    * 在ViewModel中进行多一次封装
    * */
    fun savePlace(place: Place) = Repository.savePlace(place)

    fun getSavedPlace() = Repository.getSavedPlace()

    fun isPlaceSaved() = Repository.isPlacesSaved()


    /*
    * 具体城市名,CityDao
    * */
    fun getSavedCity() = Repository.getSavedCity()

    fun isCitySaved() = Repository.isCitySaved()

    fun clearCitySaved() = Repository.clearCitySaved()

}