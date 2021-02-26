package com.sunnyweather.android.tool

import android.widget.Toast
import com.sunnyweather.android.SunnyWeatherApplication

//在String和Int类中新增一个showToast()函数
fun String.showToast( duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(SunnyWeatherApplication.context, this, duration).show()
}

fun Int.showToast(duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(SunnyWeatherApplication.context, this, duration).show()
}