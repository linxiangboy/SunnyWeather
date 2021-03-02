package com.sunnyweather.android.logic.model

data class LngLatCity(val location: String, val city: String)

data class LngLatCitydis(val location: String, val citydis: String)

data class LngLatCityDisSkyTem(val location: String, val city: String, val district: String, val skycon: String, val temperature: Int)

data class SkyconTemperatureList(val skyconTemperaturelist:List<SkyconTemperature>)

data class SkyconTemperature(val skycon: String, val temperature: Int)