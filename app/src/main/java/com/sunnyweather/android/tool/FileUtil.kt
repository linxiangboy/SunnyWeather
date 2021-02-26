package com.sunnyweather.android.tool

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder


//获取json文件内容工具类
object FileUtil {
    fun getAssetsFileText(context: Context, fileName: String): String {
        val strBuilder = StringBuilder() //创建一个字符串生成器
        val assetManager = context.assets //返回应用程序包的assetsManager实例
        val bf = BufferedReader(InputStreamReader(assetManager.open(fileName))) //读取fileName文件
        bf.use { strBuilder.append(it.readLine()) }  //读取文件内的文本，use函数finally里有执行close
        return strBuilder.toString()
    }
}