package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
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

}