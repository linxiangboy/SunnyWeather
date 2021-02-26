package com.sunnyweather.android.tool

object CharStrTool {

    fun splitString(str: String) : List<String> = str.split("&") //按照&格式分割String

    fun sqlitStringblank(str: String): String = str.replace("\\s".toRegex(), "") //去掉所有空格
}