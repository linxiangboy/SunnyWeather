package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SunnyWeatherApplication : Application() {

    companion object {

        const val TOKEN = "xqtHpNXnx4xbdE2C" //彩云天气令牌值

        const val GeoKey = "1d18680afacd7c5380e1c6fca867805b" //高德天气api key

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}

