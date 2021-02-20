package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

data class PlaceResponse(val status: String, val places: List<Place>) //query传入的是查询值

//由于json中一些字段命名可能与kotlin命名规范不一致，使用@SerializedName注解可以让json字段和kotlin字段之间建立映射关系
data class Place(val name: String, val location: Location,
                 @SerializedName("formatted_address") val address: String)

data class Location(val lng: String, val lat: String) //lng经度, lat纬度


//data class PlaceResponse(val code: String, val loaction: List<Location>)
//
//class Location(val name: String, val )
