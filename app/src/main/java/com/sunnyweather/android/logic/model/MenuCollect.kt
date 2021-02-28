package com.sunnyweather.android.logic.model

data class MenuCollect(val location: String, val city: String, val district: String, val realtime: RealtimeResponse.Realtime, val daily: DailyResponse.Daily, val homecity: String)
