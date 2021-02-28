package com.sunnyweather.android.logic.dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sunnyweather.android.tool.LogUtil

object MenuDao{

    class CreateMenuSqlDao(val context: Context, name: String, version: Int) : SQLiteOpenHelper(context, name, null, version) {

        private val createMenuCollect = "create table MenuSql (" +
                "id integer primary key autoincrement," + //id主键
                "location text," + //lnglat
                "city text," + //城市
                "district text," + //区名
                "realtime text," + //实时天气数据
                "daily text," + //未来天气数据
                "homecity text," +//true代表是主页数据 false代表不是主页数据
                "citydis text)" //city+district，是唯一的

        //只有创建时调用
        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(createMenuCollect)
        }

        //升级时调用
        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        }
    }


}