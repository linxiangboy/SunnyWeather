package com.sunnyweather.android.ui.home

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.database.getStringOrNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.databinding.FragmentHomeBinding
import com.sunnyweather.android.logic.dao.MenuDao
import com.sunnyweather.android.tool.CharStrTool
import com.sunnyweather.android.tool.LogUtil
import com.sunnyweather.android.ui.weather.WeatherActivity

//打开APP加载的第一个页面
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    /*
    * lazy 懒加载技术来获取HomeViewModel实例
    * 允许我们在整个类中随时使用viewModel这个变量而完全不用关心它们何时初始化、是否为空等前提条件
    * */
    val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(HomeViewModel::class.java)
    }

    //加载fragment时调用
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false) //加载Fragment布局
        return binding.root
    }

    //确保与fragment相关联的Activity已经创建完毕时调用
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val dbHelper = viewModel.CreateMenuSqlDao(context!!, "MenuSqlStore.db", 1)

        val db = dbHelper.writableDatabase //创建数据库

        val cursor = db.query("MenuSql", null, "homecity=?", arrayOf("true"), null, null, null)

        //如果HomeFragment被嵌入MainActivity并且收藏sql内有数据就跳转至Weather页面，并将lng/lat/city数据带过去
        //否则请求IP地址对应的城市 or 地址的经纬度城市信息
        if (activity is MainActivity && cursor.count != 0) { //如果本地有收藏的地区就执行if
            var location = ""
            var city = ""
            var district = ""
            var homecity = ""
            if (cursor.moveToFirst()) { //只显示收藏的第一行的数据
                location = cursor.getString(cursor.getColumnIndex("location"))
                city = cursor.getString(cursor.getColumnIndex("city"))
                district = cursor.getString(cursor.getColumnIndex("district"))
                homecity = cursor.getString(cursor.getColumnIndex("homecity"))
            }
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("location_lnglat", location)
                putExtra("city_name", city)
                putExtra("district_name", district)
                putExtra("collect_tag", 1) //已收藏且不允许触发点击事件
                putExtra("home_city", homecity) //将主页城市传过去，这个值在weatherActivity用来判断是否为主页城市
            }
            cursor.close()
            startActivity(intent)
            activity?.finish()
        } else {
            cursor.close()
            viewModel.getQueryIp_LngLat()//获取ip对应的地址信息
        }


        viewModel.queryIpLngLatLiveData.observe(this, Observer { result ->
            val lnglatcity = result.getOrNull()
            if (lnglatcity != null) {
                val intent = Intent(context, WeatherActivity::class.java).apply {
                    putExtra("location_lnglat", lnglatcity.location)
                    putExtra("city_name", lnglatcity.city)
                    putExtra("collect_tag", 0) //未收藏
                }
                startActivity(intent)
                activity?.finish()
            } else {
                "自动获取城市失败，请手动选择定位城市" //跳转至WeatherActivity
                result.exceptionOrNull()?.printStackTrace()
            }
        })


    }


    //当与Fragment关联的视图被移除时调用
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}