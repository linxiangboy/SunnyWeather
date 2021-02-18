package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.Exception

object Repository {

    //不允许在主线程中进行网络请求，所以要在仓库曾进行一次线程转换
    //Dispatchers.IO 所有的代码都在子线程中运行
    fun searchPlaces(query: String) = liveData(Dispatchers.IO){
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok"){ //判断服务器相应的状态时ok
                val places = placeResponse.places
                Result.success(places) //包装获取的城市数据列表
            }else{
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        }catch (e: Exception){
            Result.failure(e)
        }
        emit(result) //emit()方法将包装的结果发射出去
    }

}