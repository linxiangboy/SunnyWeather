package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sunnyweather.android.R
import com.sunnyweather.android.tool.LogUtil
import com.sunnyweather.android.tool.showToast
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.logic.model.AddressInfoPO
import com.sunnyweather.android.logic.model.PCACodePO
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork.getQueryLngLat
import com.sunnyweather.android.tool.FileUtil
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityWeatherBinding

    val viewModel by lazy {
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
        if (viewModel.location.isEmpty()){
            viewModel.location = intent.getStringExtra("location_lnglat") ?: ""
        }
        if (viewModel.cityName.isEmpty()){
            viewModel.cityName = intent.getStringExtra("city_name") ?: ""
            mBinding.now.cityName.text = viewModel.cityName //城市名数据
        }
        if (viewModel.districtName.isEmpty()){
            viewModel.districtName = intent.getStringExtra("district_name") ?: ""
            if (viewModel.districtName!="")     mBinding.now.districtName.text = viewModel.districtName //城市名数据
        }

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

        //三级城市选择器选择城市后触发
        viewModel.queryLngLatLiveData.observe(this, Observer { result ->
            val lnglat = result.getOrNull()
            if (lnglat != null) {
                viewModel.location = lnglat //存入新的lnglat
                refreshWeather() //刷新天气
                viewModel.saveLngLatCity("${viewModel.location}&${viewModel.cityName}&${viewModel.districtName}")
            } else {
                "无法成功获取天气信息".showToast()
                result.exceptionOrNull()?.printStackTrace()
            }
            mBinding.swipeRefresh.isRefreshing = false //下拉刷新结束,隐藏进度条
        })

        //三级城市选项器
        mBinding.now.selectcityLinear.setOnClickListener {
            initAddressPicker()
        }

        //收藏按钮
        mBinding.now.collectBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                "收藏".showToast()
            } else {
                "取消收藏".showToast()
            }
        }

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
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })

        mBinding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary) //设置下拉刷新进度条显示颜色
        refreshWeather() //执行一次网络请求并显示下拉控件进度条
        mBinding.swipeRefresh.setOnRefreshListener { refreshWeather() }
    }

    private fun showWeatherInfo(weather: Weather){
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

    fun refreshWeather(){ //执行一次网络请求并显示下拉控件进度条
//        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        viewModel.refreshWeather(viewModel.location)
        mBinding.swipeRefresh.isRefreshing = true
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
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
        window.statusBarColor = Color.TRANSPARENT // 将状态栏设置成透明色
    }

    private fun showAddressPicker(provinceItems: MutableList<AddressInfoPO>,
                                  cityItems: MutableList<MutableList<AddressInfoPO>>,
                                  districtItems: MutableList<MutableList<MutableList<AddressInfoPO>>>) {
        val addressPv = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, _ ->

            //点击确定按钮之后触发
            val province = provinceItems[options1].pickerViewText //省份
            val city = cityItems[options1][options2].pickerViewText//城市
            val district = districtItems[options1][options2][options3].pickerViewText//区
            if (city == "市辖区"){
                viewModel.cityName = province
                mBinding.now.cityName.text = province
            } else {
                viewModel.cityName = city
                mBinding.now.cityName.text = city
            }
            viewModel.districtName = district
            mBinding.now.districtName.text = district
            viewModel.getQueryLngLat("$province$city$district")

            }).setTitleText("请选择地区")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build<AddressInfoPO>()
        addressPv.setPicker(provinceItems, cityItems, districtItems)
        addressPv.show()
    }

    /**
     * 初始化地址数据
     */
    fun initAddressPicker() {
        val provinceItems = mutableListOf<AddressInfoPO>()
        val cityItems = mutableListOf<MutableList<AddressInfoPO>>()
        val districtItems = mutableListOf<MutableList<MutableList<AddressInfoPO>>>()
        //Json2Bean
        val pcaCodeList = Gson().fromJson<MutableList<PCACodePO>>(FileUtil.getAssetsFileText(this, "pcacode.json"), object : TypeToken<MutableList<PCACodePO>>() {}.type)
        //遍历省
        pcaCodeList.forEach {pcaCode ->
            //存放省内市区
            val cityList= mutableListOf<AddressInfoPO>()
            //存放省内所有辖区
            val areaList= mutableListOf<MutableList<AddressInfoPO>>()
            //遍历省内市区
            pcaCode.children.forEach { cCode ->
                //添加省内市区
                cityList.add(AddressInfoPO(cCode.code,cCode.name))
                //存放市内辖区
                val areas= mutableListOf<AddressInfoPO>()
                //添加市内辖区
                cCode.children.forEach {addressInfo->
                    areas.add(addressInfo)
                }
                areaList.add(areas)
            }
            //添加省份
            provinceItems.add(AddressInfoPO(pcaCode.code,pcaCode.name))
            //添加市区
            cityItems.add(cityList)
            //添加辖区
            districtItems.add(areaList)
        }
        //显示选择器
        showAddressPicker(provinceItems,cityItems,districtItems)
    }


}
