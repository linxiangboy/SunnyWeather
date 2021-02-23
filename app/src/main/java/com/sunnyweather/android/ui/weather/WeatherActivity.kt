package com.sunnyweather.android.ui.weather

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.sunnyweather.android.R
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.Tool.LogUtil
import com.sunnyweather.android.Tool.showToast
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity(), GeocodeSearch.OnGeocodeSearchListener {

    lateinit var mBinding: ActivityWeatherBinding

    val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(WeatherViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI() //沉浸式状态栏

        mBinding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        /*
        * 如果为空就先从Intent中取出经纬度坐标和地区名称
        * 并复制到WeatherViewModel相应的变量中，然后对weatherLiveData进行观察
        * 当获取到服务器返回的天气数据时，就调用showWeatherInfo()方法进行解析和展示
        * */
        if (viewModel.loactionLng.isEmpty()) {
            viewModel.loactionLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        if (viewModel.tag.isEmpty()) {
            val tag = intent.getStringExtra("tag")
            if (tag == "1") {  //根据tag0/1控制是否开启网络请求获取城市信息
                cityisEmpty() //判断要不要获取city_name和district_name
            } else {
                refershGeocode() //获取城市信息
            }
        } else {
            if (viewModel.tag == "1"){
                cityisEmpty() //判断要不要获取city_name和district_name
            } else {
                refershGeocode() //获取城市信息
            }
        }
//        if (viewModel.cityName.isEmpty()){
//            viewModel.cityName = intent.getStringExtra("city_name") ?: ""
//        }
//        if (viewModel.districtName.isEmpty()){
//            viewModel.districtName = intent.getStringExtra("district_name") ?: ""
//        }

        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                "无法成功获取天气信息".showToast()
                result.exceptionOrNull()?.printStackTrace()
            }
            mBinding.swipeRefresh.isRefreshing = false //下拉刷新结束,隐藏进度条
        })

        //打开DrawerLayout
        mBinding.now.navBtn.setOnClickListener { mBinding.drawerLayout.openDrawer(GravityCompat.START) }
        //DrawerLayout点击事件
        mBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) { //在drawerLayout状态发生切换的时候执行，一次时状态刚发生改变的时候，一次是状态改变彻底完成的时候

            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) { //在状态发生改变的时候一直执行

            }

            override fun onDrawerOpened(drawerView: View) { //drawer打开的时候执行

            }

            override fun onDrawerClosed(drawerView: View) { //drawer关闭的时候执行
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(
                    drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        })

        mBinding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary) //设置下拉刷新进度条显示颜色
        refreshWeather() //执行一次网络请求并显示下拉控件进度条
        if (!viewModel.isCitySaved()) {
            refershGeocode() //执行一次城市网络请求 第一次打开软件时加载，后续根据tag0/1控制是否开启网络请求
        }
        mBinding.swipeRefresh.setOnRefreshListener { refreshWeather() }
    }

    private fun showWeatherInfo(weather: Weather) {
        mBinding.now.cityName.text = viewModel.cityName
        mBinding.now.districtName.text = viewModel.districtName

        val realtime = weather.realtime  //RealtimeResponse.Realtime数据
        val daily = weather.daily  //DailyResponse.Daily数据

        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} °C" //当前温度
        mBinding.now.currentTemp.text = currentTempText
        mBinding.now.currentSky.text = getSky(realtime.skycon).info //getSky返回一个Sky类型的数据 取出对应的info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}" //当前温度
        mBinding.now.currentAQI.text = currentPM25Text
        mBinding.now.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg) //getSky返回一个Sky类型的数据 取出对应的bg,用setBackgroundResource()方法将背景替换

        // 填充forecast.xml布局中的数据
        mBinding.forecast.forecastLayout.removeAllViews() //删除所有子视图
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]

            val view = LayoutInflater.from(this).inflate(
                R.layout.forecast_item,
                mBinding.forecast.forecastLayout, false
            )
            val dateInfo = view.findViewById<TextView>(R.id.dateInfo)
            val skyIcon = view.findViewById<ImageView>(R.id.skyIcon)
            val skyInfo = view.findViewById<TextView>(R.id.skyInfo)
            val temperatureInfo = view.findViewById<TextView>(R.id.temperatureInfo)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value) //返回一个Sky类型的数据
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} °C"
            temperatureInfo.text = tempText
            mBinding.forecast.forecastLayout.addView(view)
        }
        //填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        mBinding.lifeIndex.coldRiskText.text = lifeIndex.coldRisk[0].desc
        mBinding.lifeIndex.dressingText.text = lifeIndex.dressing[0].desc
        mBinding.lifeIndex.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        mBinding.lifeIndex.carWashingText.text = lifeIndex.carWashing[0].desc
        mBinding.weatherLayout.visibility = View.VISIBLE
    }

    fun refreshWeather() { //执行一次网络请求并显示下拉控件进度条
        viewModel.refreshWeather(viewModel.loactionLng, viewModel.locationLat)

        mBinding.swipeRefresh.isRefreshing = true
    }

    fun refershGeocode() { //执行高德地图搜索功能获取城市和地区名 - 逆地理编译
        val geocodeSearch = GeocodeSearch(this)

        geocodeSearch.setOnGeocodeSearchListener(this)

        val query = RegeocodeQuery( //LatLonPoint(lat, lng)
            LatLonPoint(
                viewModel.locationLat.toDouble(),
                viewModel.loactionLng.toDouble()
            ), 200F, GeocodeSearch.GPS
        )

        geocodeSearch.getFromLocationAsyn(query)
    }


    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
//            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            //沉浸式状态栏(非透明)
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
        window.statusBarColor = Color.TRANSPARENT // 将状态栏设置成透明色
    }


    override fun onRegeocodeSearched(p0: RegeocodeResult?, p1: Int) { // 获取地址描述信息
        if (p1 == 1000) {
            val city = p0?.regeocodeAddress?.city.toString()
            val district = p0?.regeocodeAddress?.district.toString()

            viewModel.cityName = city
            viewModel.districtName = district

            viewModel.saveCity(city, district)
            LogUtil.d("yyy", "高德地图城市获取成功 + $city $district")
            viewModel.tag = "0"
        } else {
            LogUtil.d("yyy", "高德地图城市获取失败")
        }
    }

    //获取坐标信息
    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {}

    private fun cityisEmpty() { //判断要不要获取city_name和district_name
        if (viewModel.cityName.isEmpty()) {
            viewModel.cityName = intent.getStringExtra("city_name") ?: ""
        }
        if (viewModel.districtName.isEmpty()) {
            viewModel.districtName = intent.getStringExtra("district_name") ?: ""
        }
    }
}