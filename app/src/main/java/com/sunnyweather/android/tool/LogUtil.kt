package com.sunnyweather.android.tool

import android.util.Log

object LogUtil {

    private const val VERBOSE = 1 //打印全部日志

    private const val DEBUG = 2

    private const val INFO = 3

    private const val WARN = 4

    private const val ERROR = 5 //只打印错误日志

    private var level = VERBOSE

    fun v(tag: String, msg: String){
        if (level <= VERBOSE){
            Log.v(tag, msg)
        }
    }

    fun d(msg: String, tag: String = "yyy"){
        if (level <= DEBUG){
            Log.d(tag, msg)
        }
    }

    fun i(tag: String, msg: String){
        if (level <= INFO){
            Log.i(tag, msg)
        }
    }

    fun w(tag: String, msg: String){
        if (level <= WARN){
            Log.w(tag, msg)
        }
    }

    fun e(tag: String, msg: String){
        if (level <= ERROR){
            Log.e(tag, msg)
        }
    }

}