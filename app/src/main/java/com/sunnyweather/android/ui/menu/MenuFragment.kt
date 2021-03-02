package com.sunnyweather.android.ui.menu

import android.content.ContentValues
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sunnyweather.android.databinding.FragmentMenuBinding
import com.sunnyweather.android.logic.model.*
import com.sunnyweather.android.tool.LogUtil
import com.sunnyweather.android.tool.showToast


class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MenuAdapter

    /*
    * lazy 懒加载技术来获取PlaceViewModel实例
    * 允许我们在整个类中随时使用viewModel这个变量而完全不用关心它们何时初始化、是否为空等前提条件
    * */
    val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(MenuViewModel::class.java)
    }

    val dbHelper by lazy {
        viewModel.CreateMenuSqlDao(context!!, "MenuSqlStore.db", 1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMenuBinding.inflate(inflater, container, false) //加载Fragment布局
        return binding.root
    }

    //确保与fragment相关联的Activity已经创建完毕时调用
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        //刷新sqlite内的数据
        viewModel.weatherListLiveData.observe(this, Observer { result ->
            val disweatherlist = result.getOrNull() //不为空
            if (disweatherlist != null) {
                setMenuSql(disweatherlist) //更新数据库内的天气数据
            } else {
                "无法成功获取天气信息".showToast()
                result.exceptionOrNull()?.printStackTrace()
            }
        })

        val layoutManager = LinearLayoutManager(activity)
        //给RecyclerView设置和layoutManager和适配器,并使用了viewModel.menulist集合作为数据源
        _binding?.recyclerView?.layoutManager = layoutManager
        adapter = MenuAdapter(this, viewModel.menulist)
        _binding?.recyclerView?.adapter = adapter

        /*
        * 1、判空
        * 2、不为空获取locationlist发起请求
        * 3、请求成功在viewModel.liveData内更新数据库
        * 4、refreshAdapter取出数据并刷新adapter
        * */

        if (!getSqlorNull()) { //除了主页城市之外还收藏有其他城市 false不为空
            viewModel.refreshWeatherList(getLngLatMenuSql()) //取出数据库内除了主页城市之外的lnglat数据并发起请求
        }

    }

    //当与Fragment关联的视图被移除时调用
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //刷新adapter
    fun refreshAdapter() {
        getMenuSql()
        adapter.notifyDataSetChanged()
    }

    //查询并取出sql内的数据
    fun getMenuSql() {
        val db = dbHelper.writableDatabase

        val cursor = db.query("MenuSql", null, null, null, null, null, null)
        if (cursor.count == 0) {
            _binding?.recyclerView?.visibility = View.GONE //adapter隐藏
            _binding?.bgImaeView?.visibility = View.VISIBLE //默认背景显示
        } else {
            _binding?.recyclerView?.visibility = View.VISIBLE //adapter显示
            _binding?.bgImaeView?.visibility = View.GONE //默认背景隐藏
        }
        viewModel.menulist.clear()
        if (cursor.moveToFirst()) {
            do {
                val location = cursor.getString(cursor.getColumnIndex("location"))
                val city = cursor.getString(cursor.getColumnIndex("city"))
                val district = cursor.getString(cursor.getColumnIndex("district"))
                val realtime = cursor.getString(cursor.getColumnIndex("realtime"))
                val daily = cursor.getString(cursor.getColumnIndex("daily"))
                val homecity = cursor.getString(cursor.getColumnIndex("homecity"))
                viewModel.menulist.add(
                    MenuCollect(
                        location, city, district,
                        Gson().fromJson(realtime, RealtimeResponse.Realtime::class.java),
                        Gson().fromJson(daily, DailyResponse.Daily::class.java), homecity
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    //数据库内是否为空，如果为空返回true,不为空返回false
    fun getSqlorNull(): Boolean {
        val db = dbHelper.writableDatabase

        val cursor = db.query("MenuSql", null, null, null, null, null, null)
        return cursor.count == 0
    }

    //获取location list
    fun getLngLatMenuSql(): MutableList<LngLatCitydis> {
        var locationlist = mutableListOf<LngLatCitydis>()
        val db = dbHelper.writableDatabase
        val cursor = db.query("MenuSql", null, "homecity=?", arrayOf("false"), null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val location = cursor.getString(cursor.getColumnIndex("location"))
                val citydis = cursor.getString(cursor.getColumnIndex("citydis"))
                locationlist.add(LngLatCitydis(location, citydis))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return locationlist
    }

    //更新数据库内的realtime和daily
    fun setMenuSql(disweatherlist: MutableList<CitydisWeather>) {
        Thread{
            val db = dbHelper.writableDatabase

            var values = ContentValues()
            for (disweather in disweatherlist){

                values.put("realtime", Gson().toJson(disweather.realtime))
                values.put("daily", Gson().toJson(disweather.daily))
                db.update("MenuSql", values, "citydis=?", arrayOf("${disweather.citydis}"))
            }

//            val cursor = db.query("MenuSql", null, "homecity=?", arrayOf("false"), null, null, null)
//            if (cursor.moveToFirst()){
//                do {
//                    val values = ContentValues()
//                    values.put("realtime", Gson().toJson(weatherlist[cursor.position].realtime))
//                    values.put("daily", Gson().toJson(weatherlist[cursor.position].daily))
//                    LogUtil.d("${cursor.position} ${cursor.columnCount} ${Gson().toJson(weatherlist[cursor.position].realtime.skycon)}")
//                    db.update("MenuSql", values, null, null)
//                    }while (cursor.moveToNext())
//            }
//            cursor.close()
        }.start()

        refreshAdapter() //刷新界面
    }


}