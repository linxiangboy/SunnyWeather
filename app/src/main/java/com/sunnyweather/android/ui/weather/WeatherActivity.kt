package com.sunnyweather.android.ui.weather

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.Tool.LogUtil
import com.sunnyweather.android.Tool.showToast
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityWeatherBinding

    private val viewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(WeatherViewModel::class.java)
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
        if (viewModel.loactionLng.isEmpty()){
            viewModel.loactionLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()){
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()){
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                "无法成功获取天气信息".showToast()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
        //执行一次刷新天气的请求
        viewModel.refreshWeather(viewModel.loactionLng, viewModel.locationLat)
    }

    private fun showWeatherInfo(weather: Weather){
        mBinding.now.placeName.text = viewModel.placeName //城市名数据

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
        for (i in 0 until days){
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]

            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                mBinding.forecast.forecastLayout, false)
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


    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
        window.statusBarColor = Color.TRANSPARENT // 将状态栏设置成透明色
    }
}