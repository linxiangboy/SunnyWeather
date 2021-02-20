package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SunnyWeatherApplication : Application() {

    companion object {

        const val TOKEN = "xqtHpNXnx4xbdE2C" //彩云天气令牌值

        const val KEY = "a261dd713b1543158760e3a58b2b1c5e" //和风天气key

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}

